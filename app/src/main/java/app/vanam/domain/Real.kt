package app.vanam.domain
import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp

/** Looks in app assets first, then the app's external files folder. */
internal fun readModel(ctx: Context, name: String): ByteBuffer? {
    val bytes = runCatching { ctx.assets.open(name).use { it.readBytes() } }.getOrNull()
        ?: ctx.getExternalFilesDir(null)?.let { File(it, name) }?.takeIf { it.exists() }?.readBytes()
        ?: return null
    return ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply { put(bytes); rewind() }
}

/** BirdNET V2.4: 3 s at 48 kHz in, 6522 logits out. Scores every bird label, not just the seeded ones. Falls back to the fake if the model is missing. */
@Singleton class BirdNetAudioIdentifier @Inject constructor(@ApplicationContext private val ctx: Context, private val fallback: FakeAudioIdentifier) : AudioIdentifier {
    private class Loaded(val interp: Interpreter, val birds: List<BirdLabel>, val n: Int)
    private val loaded: Loaded? by lazy {
        runCatching {
            val buf = readModel(ctx, "birdnet.tflite") ?: return@runCatching null
            val labels = BirdLabels.all(ctx)
            Loaded(Interpreter(buf, Interpreter.Options().apply { setNumThreads(2) }), labels.filter(BirdLabels::isBird), labels.size).also { ModelStatus.audio = "BirdNET" }
        }.getOrNull()
    }
    override suspend fun identify(pcm: ShortArray, sampleRate: Int): List<Candidate> = withContext(Dispatchers.Default) {
        val l = loaded ?: run { ModelStatus.audio = "Fake (birdnet.tflite missing)"; return@withContext fallback.identify(pcm, sampleRate) }
        val win = 144000; val x = FloatArray(pcm.size) { pcm[it] / 32768f }
        val starts = if (x.size <= win) listOf(0) else listOf(0, (x.size - win) / 2, x.size - win).distinct()
        val best = FloatArray(l.n)
        for (s in starts) {
            val inp = Array(1) { FloatArray(win) }; System.arraycopy(x, s, inp[0], 0, minOf(win, x.size - s))
            val out = Array(1) { FloatArray(l.n) }
            synchronized(l) { l.interp.run(inp, out) }
            for (b in l.birds) { val p = 1f / (1f + exp(-out[0][b.index].coerceIn(-15f, 15f))); if (p > best[b.index]) best[b.index] = p }
        }
        l.birds.sortedByDescending { best[it.index] }.take(3).map { Candidate(BirdLabels.idFor(it), best[it.index]) }
    }
}

/** Quantized or float classifier. labels file = species ids, one per line. Falls back to the fake if missing. */
@Singleton class TfliteImageIdentifier @Inject constructor(@ApplicationContext private val ctx: Context, private val fallback: FakeImageIdentifier) : ImageIdentifier {
    private class Loaded(val interp: Interpreter, val labels: List<String>, val size: Int, val quantIn: Boolean, val quantOut: Boolean)
    private val loaded: Loaded? by lazy {
        runCatching {
            val buf = readModel(ctx, "species_classifier.tflite") ?: return@runCatching null
            val labels = (runCatching { ctx.assets.open("species_labels.txt").bufferedReader().readLines() }.getOrNull()
                ?: File(ctx.getExternalFilesDir(null), "species_labels.txt").readLines()).filter { it.isNotBlank() }
            val it = Interpreter(buf)
            Loaded(it, labels, it.getInputTensor(0).shape()[1], it.getInputTensor(0).dataType() == DataType.UINT8,
                it.getOutputTensor(0).dataType() == DataType.UINT8).also { ModelStatus.image = "TFLite classifier" }
        }.getOrNull()
    }
    override suspend fun identify(bitmap: Bitmap): List<Candidate> = withContext(Dispatchers.Default) {
        val l = loaded ?: run { ModelStatus.image = "Fake (species_classifier.tflite missing)"; return@withContext fallback.identify(bitmap) }
        val bm = Bitmap.createScaledBitmap(bitmap, l.size, l.size, true)
        val px = IntArray(l.size * l.size); bm.getPixels(px, 0, l.size, 0, 0, l.size, l.size)
        val inp = ByteBuffer.allocateDirect(px.size * 3 * (if (l.quantIn) 1 else 4)).order(ByteOrder.nativeOrder())
        for (p in px) for (sh in intArrayOf(16, 8, 0)) { val v = (p shr sh) and 0xFF; if (l.quantIn) inp.put(v.toByte()) else inp.putFloat((v - 127.5f) / 127.5f) }
        inp.rewind()
        val probs = FloatArray(l.labels.size)
        synchronized(l) {
            if (l.quantOut) { val o = Array(1) { ByteArray(probs.size) }; l.interp.run(inp, o); for (i in probs.indices) probs[i] = (o[0][i].toInt() and 0xFF) / 255f }
            else { val o = Array(1) { FloatArray(probs.size) }; l.interp.run(inp, o); o[0].copyInto(probs) }
        }
        probs.indices.sortedByDescending { probs[it] }.take(3).map { Candidate(l.labels[it], probs[it]) }
    }
}

/** On-device Gemma via MediaPipe LLM Inference. Model file: gemma.task. Template fallback if missing, slow, or off-script. */
@Singleton class GemmaFieldWriter @Inject constructor(@ApplicationContext private val ctx: Context, private val fallback: TemplateFieldWriter) : FieldWriter {
    private val llm: LlmInference? by lazy {
        runCatching {
            val f = listOfNotNull(ctx.getExternalFilesDir(null)?.let { File(it, "gemma.task") }, File(ctx.filesDir, "gemma.task")).firstOrNull { it.exists() }
                ?: return@runCatching null
            LlmInference.createFromOptions(ctx, LlmInference.LlmInferenceOptions.builder().setModelPath(f.absolutePath).setMaxTokens(160).build())
                .also { ModelStatus.writer = "Gemma" }
        }.getOrNull()
    }
    private val scope = CoroutineScope(Dispatchers.Default)
    override suspend fun write(ctx: FieldContext): FieldNote {
        val s = ctx.species; val m = llm
        if (s == null || m == null || s.fieldMarks.isBlank()) { if (m == null) ModelStatus.writer = "Template (gemma.task missing)"; return fallback.write(ctx) }
        val part = if (ctx.hourOfDay < 10) "morning" else if (ctx.hourOfDay < 17) "midday" else "evening"
        val prompt = "You are a calm field guide. Use ONLY the facts below. Write at most two short sentences. Add no other facts and no numbers.\n" +
            "Species: ${s.common}\nField marks: ${s.fieldMarks}\nHabitat: ${s.habitat}\nSeason: ${s.season}\nTime of day: $part\nSeen before on this walk: ${s.id in ctx.alreadyFound}\n"
        val job = scope.async { runCatching { m.generateResponse(prompt) }.getOrNull() }
        val text = withTimeoutOrNull(6000) { job.await() }?.trim()
        if (text.isNullOrBlank() || text.length > 280 || text.any { it.isDigit() }) return fallback.write(ctx)
        return FieldNote(text, s.confirmCue)
    }
}
