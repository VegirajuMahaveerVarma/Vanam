package app.vanam.domain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class LogEntry(val id: Long, val kind: String, val model: String, val latencyMs: Long, val top: String, val conf: Float, val verdict: String, val correct: Boolean?)
/** In-memory only. Read by the hidden debug screen. */
object InferenceLog {
    private val _e = MutableStateFlow<List<LogEntry>>(emptyList()); val entries: StateFlow<List<LogEntry>> = _e
    private var n = 0L
    fun add(kind: String, model: String, ms: Long, top: String, conf: Float, verdict: String) =
        _e.update { it + LogEntry(n++, kind, model, ms, top, conf, verdict, null) }
    fun mark(id: Long, ok: Boolean) = _e.update { l -> l.map { if (it.id == id) it.copy(correct = ok) else it } }
    fun clear() { _e.value = emptyList() }
}
object ModelStatus { @Volatile var image = "Fake"; @Volatile var audio = "Fake"; @Volatile var writer = "Template" }
