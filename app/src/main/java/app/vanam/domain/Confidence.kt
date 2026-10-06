package app.vanam.domain

/** The honesty rule: never fake certainty. Thresholds from the product spec. */
sealed interface Verdict {
    data class Sure(val c: Candidate) : Verdict
    data class Probably(val c: Candidate) : Verdict
    data object NotSure : Verdict
}
object ConfidencePolicy {
    const val SURE = 0.75f; const val PROBABLY = 0.4f
    fun judge(cands: List<Candidate>): Verdict {
        val top = cands.maxByOrNull { it.confidence } ?: return Verdict.NotSure
        return when {
            top.confidence >= SURE -> Verdict.Sure(top)
            top.confidence >= PROBABLY -> Verdict.Probably(top)
            else -> Verdict.NotSure
        }
    }
    fun word(c: Float) = when { c >= SURE -> "Sure"; c >= PROBABLY -> "Probably"; else -> "Not sure" }
}
