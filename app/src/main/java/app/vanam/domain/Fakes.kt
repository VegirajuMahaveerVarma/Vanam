package app.vanam.domain
import android.graphics.Bitmap
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

private val birds = listOf("red_vented_bulbul","indian_robin","purple_sunbird","indian_peafowl","common_myna","rose_ringed_parakeet","asian_koel","oriental_magpie_robin")
private val plants = listOf("neem","tamarind","gulmohar","flame_of_the_forest","banyan","peepal","pongamia")
private fun fake(pool: List<String>): List<Candidate> {
    val conf = listOf(0.93f, 0.84f, 0.78f, 0.66f, 0.52f, 0.31f, 0.18f).random()
    val a = pool.random(); val b = pool.filter { it != a }.random()
    return listOf(Candidate(a, conf), Candidate(b, Random.nextFloat() * 0.2f))
}
class FakeImageIdentifier @Inject constructor() : ImageIdentifier {
    override suspend fun identify(bitmap: Bitmap): List<Candidate> { delay(900); return fake(birds + plants) }
}
class FakeAudioIdentifier @Inject constructor() : AudioIdentifier {
    override suspend fun identify(pcm: ShortArray, sampleRate: Int): List<Candidate> { delay(1200); return fake(birds) }
}
/** Grounded ONLY in DB facts plus time of day and what is already found. Never invents. Also the Phase 2 fallback. */
class TemplateFieldWriter @Inject constructor() : FieldWriter {
    override suspend fun write(ctx: FieldContext): FieldNote {
        val s = ctx.species ?: return FieldNote("A quiet moment. Look around slowly.", null)
        val part = if (ctx.hourOfDay < 10) "Morning" else if (ctx.hourOfDay < 17) "Midday" else "Evening"
        val lead = if (s.id in ctx.alreadyFound) "Another sighting." else "$part find."
        if (s.fieldMarks.isBlank()) return FieldNote("$lead ${s.common}, ${s.scientific}.", s.confirmCue)
        return FieldNote("$lead ${s.fieldMarks.substringBefore('.')}.", s.confirmCue)
    }
}
