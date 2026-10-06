package app.vanam.domain
import android.graphics.Bitmap

data class Candidate(val speciesId: String, val confidence: Float)
data class FieldNote(val blurb: String, val challenge: String?)
data class FieldContext(val species: SpeciesFacts?, val hourOfDay: Int, val month: Int, val alreadyFound: List<String>)
data class SpeciesFacts(
    val id: String, val common: String, val telugu: String?, val scientific: String, val type: String,
    val fieldMarks: String, val habitat: String, val season: String, val confirmCue: String, val art: String?)

interface ImageIdentifier { suspend fun identify(bitmap: Bitmap): List<Candidate> }
interface AudioIdentifier { suspend fun identify(pcm: ShortArray, sampleRate: Int): List<Candidate> }
interface FieldWriter { suspend fun write(ctx: FieldContext): FieldNote }
