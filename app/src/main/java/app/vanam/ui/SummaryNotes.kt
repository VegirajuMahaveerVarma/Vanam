@file:OptIn(ExperimentalMaterial3Api::class)
package app.vanam.ui
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable fun Stat(k: String, v: String) { Column(Modifier.padding(vertical = 8.dp)) { Label(k); Text(v, style = MaterialTheme.typography.headlineSmall) } }

@Composable fun SummaryScreen(onHome: () -> Unit, onNotes: () -> Unit, vm: SummaryViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState(); val ctx = LocalContext.current
    PaperScreen {
        Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
            Label("Trail complete"); Text("Well walked.", style = MaterialTheme.typography.displaySmall)
            SketchDivider(Modifier.padding(vertical = 12.dp))
            s?.let {
                Stat("Species", "${it.species}"); Stat("Birds / plants", "${it.birds} / ${it.plants}"); Stat("Distance", fmtDist(it.meters))
                Stat("Challenges done", "${it.challenges}"); Stat("Time outside", fmtDur(it.durMs)); Stat("Screen time saved", fmtDur(it.savedMs))
                Spacer(Modifier.height(20.dp))
                Button({ shareSummary(ctx, it) }, Modifier.fillMaxWidth().heightIn(min = 64.dp), shape = RoundedCornerShape(24.dp)) { Text("Share as image") }
            }
            OutlinedButton(onNotes, Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(top = 8.dp), shape = RoundedCornerShape(24.dp)) { Text("Field Notes") }
            TextButton(onHome, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Home") }
        }
    }
}

@Composable fun NotesScreen(onBack: () -> Unit, vm: NotesViewModel = hiltViewModel()) {
    val trails by vm.state.collectAsState(); val df = remember { SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault()) }
    PaperScreen {
        Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
            Label("Field notes"); Text("Your walks", style = MaterialTheme.typography.displaySmall)
            SketchDivider(Modifier.padding(vertical = 12.dp))
            if (trails.isEmpty()) { BirdArt(Modifier.size(96.dp), MaterialTheme.colorScheme.primary); Spacer(Modifier.height(8.dp)); Text("No trails yet. Go find something small.", style = MaterialTheme.typography.bodyLarge) }
            trails.forEach { nt ->
                Label("${df.format(Date(nt.t.startMs))} · ${fmtDist(nt.t.distanceM)}", color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(8.dp))
                val pts = nt.items.mapNotNull { i -> if (i.d.lat != null && i.d.lon != null) i.d.lat to i.d.lon else null }
                if (pts.isNotEmpty()) { MiniMap(pts); Spacer(Modifier.height(8.dp)) }
                if (nt.items.isEmpty()) Text("Nothing saved on this walk. That is fine too.", style = MaterialTheme.typography.bodyLarge)
                nt.items.forEach { Entry(it); Spacer(Modifier.height(12.dp)) }
                SketchDivider(Modifier.padding(vertical = 14.dp))
            }
            TextButton(onBack, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Back") }
        }
    }
}

@Composable fun Entry(n: NoteItem) {
    val bmp = remember(n.d.photoPath) { n.d.photoPath?.let { loadPhoto(it) } }
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(18.dp)) {
            bmp?.let { Image(it.asImageBitmap(), "Photo of ${n.sp?.common ?: "the sighting"}", Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop); Spacer(Modifier.height(12.dp)) }
            Text(n.sp?.common ?: n.d.speciesId, style = MaterialTheme.typography.headlineSmall)
            n.sp?.telugu?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary) }
            if (n.d.lat != null && n.d.lon != null) {
                val ns = if (n.d.lat >= 0) "N" else "S"; val ew = if (n.d.lon >= 0) "E" else "W"
                Label("%.4f°%s %.4f°%s".format(Locale.US, kotlin.math.abs(n.d.lat), ns, kotlin.math.abs(n.d.lon), ew))
            }
            if (n.d.note.isNotBlank()) { Spacer(Modifier.height(6.dp)); Text(n.d.note, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
