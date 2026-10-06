package app.vanam.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vanam.domain.InferenceLog
import app.vanam.domain.ModelStatus

/** Hidden: debug builds only. Long-press the "Vanam" title on Home. Mark each audio/image result right or wrong to get a rough accuracy. */
@Composable fun DebugScreen(onBack: () -> Unit) {
    val entries by InferenceLog.entries.collectAsState()
    PaperScreen {
        Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
            Label("Debug · hidden"); Text("Inference log", style = MaterialTheme.typography.displaySmall)
            Label("Image: ${ModelStatus.image}"); Label("Audio: ${ModelStatus.audio}"); Label("Writer: ${ModelStatus.writer}")
            SketchDivider(Modifier.padding(vertical = 10.dp))
            listOf("image", "audio", "writer").forEach { k ->
                val es = entries.filter { it.kind == k }
                if (es.isNotEmpty()) {
                    val marked = es.filter { it.correct != null }
                    val acc = if (marked.isEmpty() || k == "writer") "n/a" else "${marked.count { it.correct == true } * 100 / marked.size}%"
                    Label("$k · n=${es.size} · avg ${es.map { it.latencyMs }.average().toLong()} ms · accuracy $acc", color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(Modifier.height(8.dp))
            entries.asReversed().forEach { e ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${e.kind} · ${e.top}", style = MaterialTheme.typography.bodyLarge)
                        Label("${e.model} · ${e.latencyMs} ms · ${"%.2f".format(e.conf)} · ${e.verdict}")
                    }
                    if (e.kind != "writer") {
                        TextButton({ InferenceLog.mark(e.id, true) }) { Text(if (e.correct == true) "✓ right" else "right") }
                        TextButton({ InferenceLog.mark(e.id, false) }) { Text(if (e.correct == false) "✗ wrong" else "wrong") }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                TextButton(InferenceLog::clear, Modifier.weight(1f).heightIn(min = 56.dp)) { Text("Clear") }
                TextButton(onBack, Modifier.weight(1f).heightIn(min = 56.dp)) { Text("Back") }
            }
        }
    }
}
