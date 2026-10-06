package app.vanam.domain
import android.content.Context
import app.vanam.data.Seed

/** One BirdNET output label: its index in the model output, scientific name and common name. */
data class BirdLabel(val index: Int, val scientific: String, val common: String)

/** All 6,522 BirdNET labels. Seeded birds keep their own id; every other bird gets the id "bn:<scientific name>". */
object BirdLabels {
    const val PREFIX = "bn:"
    private val NOT_BIRDS = setOf("Dog", "Engine", "Environmental", "Fireworks", "Gun", "Noise", "Siren",
        "Human non-vocal", "Human vocal", "Human whistle", "Power tools")
    @Volatile private var cache: List<BirdLabel>? = null
    fun all(ctx: Context): List<BirdLabel> = cache ?: synchronized(this) {
        cache ?: ctx.assets.open("birdnet_labels.txt").bufferedReader().readLines().mapIndexed { i, line ->
            val sci = line.substringBefore('_'); BirdLabel(i, sci, line.substringAfter('_', sci))
        }.also { cache = it }
    }
    fun isBird(l: BirdLabel) = l.scientific !in NOT_BIRDS && ' ' in l.scientific
    fun idFor(l: BirdLabel): String =
        Seed.species.firstOrNull { it.type == "bird" && it.scientific == l.scientific }?.id ?: (PREFIX + l.scientific)
    fun find(ctx: Context, id: String): BirdLabel? =
        if (id.startsWith(PREFIX)) all(ctx).firstOrNull { it.scientific == id.removePrefix(PREFIX) } else null
}
