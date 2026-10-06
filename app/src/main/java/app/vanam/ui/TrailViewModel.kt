package app.vanam.ui
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vanam.data.*
import app.vanam.domain.*
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

data class Sighting(val facts: SpeciesFacts?, val verdict: Verdict, val note: FieldNote?, val photo: String?, val conf: Float,
                    val via: String, val lat: Double?, val lon: Double?)
data class TrailUi(val elapsedMs: Long = 0, val distanceM: Float = 0f, val listening: Boolean = false, val amp: Float = 0f,
                   val busy: Boolean = false, val sighting: Sighting? = null, val challenge: ChallengeEntity? = null, val done: Int = 0,
                   val found: Int = 0, val pocket: Boolean = false, val camera: Boolean = false, val buzz: Int = 0, val finishedId: Long? = null)

@HiltViewModel
class TrailViewModel @Inject constructor(
    @ApplicationContext ctx: Context, private val repo: Repo, private val dao: VanamDao,
    private val img: ImageIdentifier, private val aud: AudioIdentifier, private val writer: FieldWriter
) : ViewModel() {
    private val SR = 48000
    private val _ui = MutableStateFlow(TrailUi()); val ui: StateFlow<TrailUi> = _ui
    private val start = System.currentTimeMillis()
    private var trailId = 0L; private var fgMs = 0L; private var resumed = true; private var finishing = false
    private var challenges = emptyList<ChallengeEntity>(); private val foundIds = linkedSetOf<String>()
    private var meters = 0f; private var last: Location? = null; private var fix: Location? = null; private var locating = false
    private val fused = LocationServices.getFusedLocationProviderClient(ctx)
    private val cb = object : LocationCallback() {
        override fun onLocationResult(r: LocationResult) {
            for (l in r.locations) {
                if (l.hasAccuracy() && l.accuracy > 35f) continue
                fix = l
                val p = last
                if (p == null) { last = l; continue }
                val d = p.distanceTo(l)
                if (d >= 3f) { if (d <= 200f) meters += d; last = l }
            }
            _ui.update { it.copy(distanceM = meters) }
        }
    }
    private val setup = viewModelScope.launch {
        repo.ensureSeeded(); challenges = dao.challenges()
        trailId = dao.insertTrail(TrailEntity(startMs = start, endMs = null, distanceM = 0f, foregroundMs = 0, challengesDone = 0))
    }
    init {
        viewModelScope.launch {
            while (true) { delay(1000); if (resumed && !_ui.value.pocket) fgMs += 1000; _ui.update { it.copy(elapsedMs = System.currentTimeMillis() - start) } }
        }
    }
    fun setForeground(b: Boolean) { resumed = b }
    fun setPocket(b: Boolean) = _ui.update { it.copy(pocket = b) }
    fun setCamera(b: Boolean) = _ui.update { it.copy(camera = b) }

    @SuppressLint("MissingPermission")
    fun startLocation() {
        if (locating) return; locating = true
        fused.requestLocationUpdates(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L).setMinUpdateDistanceMeters(3f).build(), cb, Looper.getMainLooper())
    }
    fun onPhoto(path: String) {
        _ui.update { it.copy(camera = false, busy = true) }
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 }) }
            val t0 = System.nanoTime()
            val c = if (bmp != null) img.identify(bmp) else emptyList()
            logIt("image", ModelStatus.image, t0, c)
            present(c, path, "look")
        }
    }
    fun listen() {
        if (_ui.value.listening || _ui.value.busy) return
        viewModelScope.launch {
            _ui.update { it.copy(listening = true) }
            val pcm = withContext(Dispatchers.IO) { record() }
            _ui.update { it.copy(listening = false, busy = true, amp = 0f) }
            val t0 = System.nanoTime()
            val c = if (pcm.isEmpty()) emptyList() else aud.identify(pcm, SR)
            logIt("audio", ModelStatus.audio, t0, c)
            present(c, null, "listen")
        }
    }
    @SuppressLint("MissingPermission")
    private fun record(): ShortArray {
        val total = SR * 5
        val minBuf = AudioRecord.getMinBufferSize(SR, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        fun mk(src: Int) = AudioRecord(src, SR, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, SR))
        var rec = mk(MediaRecorder.AudioSource.UNPROCESSED)  // no phone noise suppression or gain; BirdNET works best on raw audio
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); rec = mk(MediaRecorder.AudioSource.MIC) }
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); return ShortArray(0) }
        val out = ShortArray(total); var n = 0; val chunk = ShortArray(SR / 10)
        try {
            rec.startRecording()
            while (n < total) {
                val r = rec.read(chunk, 0, minOf(chunk.size, total - n)); if (r <= 0) break
                System.arraycopy(chunk, 0, out, n, r); n += r
                var peak = 0; for (i in 0 until r) peak = maxOf(peak, abs(chunk[i].toInt()))
                _ui.update { it.copy(amp = peak / 32768f) }
            }
        } finally { runCatching { rec.stop() }; rec.release() }
        return out
    }
    private fun logIt(kind: String, model: String, t0: Long, c: List<Candidate>) {
        val top = c.maxByOrNull { it.confidence }
        val v = when (ConfidencePolicy.judge(c)) { is Verdict.Sure -> "sure"; is Verdict.Probably -> "probably"; is Verdict.NotSure -> "not sure" }
        InferenceLog.add(kind, model, (System.nanoTime() - t0) / 1_000_000, top?.speciesId ?: "-", top?.confidence ?: 0f, v)
    }
    private suspend fun present(c: List<Candidate>, photo: String?, via: String) {
        val v = ConfidencePolicy.judge(c)
        val top = when (v) { is Verdict.Sure -> v.c; is Verdict.Probably -> v.c; is Verdict.NotSure -> null }
        val facts = top?.let { repo.facts(it.speciesId) }
        val cal = Calendar.getInstance()
        val t0 = System.nanoTime()
        val note = facts?.let { writer.write(FieldContext(it, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MONTH) + 1, foundIds.toList())) }
        if (facts != null) InferenceLog.add("writer", ModelStatus.writer, (System.nanoTime() - t0) / 1_000_000, facts.id, 0f, "-")
        val s = Sighting(facts, v, note, photo, c.maxOfOrNull { it.confidence } ?: 0f, via, fix?.latitude, fix?.longitude)
        _ui.update { it.copy(busy = false, sighting = s) }
    }
    fun dismissSighting() = _ui.update { it.copy(sighting = null) }
    fun save() {
        val s = _ui.value.sighting ?: return; val f = s.facts ?: return
        viewModelScope.launch {
            setup.join()
            dao.insertDiscovery(DiscoveryEntity(trailId = trailId, speciesId = f.id, confidence = s.conf, lat = s.lat, lon = s.lon,
                photoPath = s.photo, note = s.note?.blurb ?: "", atMs = System.currentTimeMillis()))
            foundIds.add(f.id); _ui.update { it.copy(sighting = null, found = foundIds.size) }
        }
    }
    fun explore() {
        val cal = Calendar.getInstance(); val h = cal.get(Calendar.HOUR_OF_DAY)
        val tod = if (h < 10) "morning" else if (h < 17) "day" else "evening"
        val season = when (cal.get(Calendar.MONTH) + 1) { 3, 4, 5 -> "summer"; 6, 7, 8, 9 -> "monsoon"; else -> "winter" }
        val cur = _ui.value.challenge?.id
        val pool = challenges.filter { (it.timeOfDay == "any" || it.timeOfDay == tod) && (it.season == "any" || it.season == season) && it.id != cur }
        _ui.update { it.copy(challenge = pool.randomOrNull()) }
    }
    fun skipChallenge() = _ui.update { it.copy(challenge = null) }
    fun completeChallenge() = _ui.update { it.copy(challenge = null, done = it.done + 1, buzz = it.buzz + 1) }
    fun finish() {
        if (finishing) return; finishing = true
        viewModelScope.launch {
            setup.join(); fused.removeLocationUpdates(cb)
            dao.updateTrail(TrailEntity(trailId, start, System.currentTimeMillis(), meters, fgMs, _ui.value.done))
            _ui.update { it.copy(finishedId = trailId) }
        }
    }
    override fun onCleared() { fused.removeLocationUpdates(cb) }
}
