@file:OptIn(ExperimentalMaterial3Api::class)
package app.vanam.ui
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import app.vanam.BuildConfig
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable fun HomeScreen(onStart: () -> Unit, onPack: () -> Unit, onNotes: () -> Unit, onDebug: () -> Unit = {}, vm: HomeViewModel = hiltViewModel()) {
    val last by vm.last.collectAsState(); val pack by vm.pack.collectAsState()
    PaperScreen {
        Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Vanam", Modifier.pointerInput(Unit) { if (BuildConfig.DEBUG) detectTapGestures(onLongPress = { onDebug() }) }, style = MaterialTheme.typography.displaySmall); Spacer(Modifier.weight(1f))
                BirdArt(Modifier.size(52.dp), MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp))
                LeafArt(Modifier.size(40.dp), MaterialTheme.colorScheme.secondary)
            }
            Text("AI field guide. No signal required.", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp)); SketchDivider(); Spacer(Modifier.height(24.dp))
            Surface(onPack, Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(22.dp)) {
                    Label("Nature pack")
                    Text(if (pack) "Telangana Nature Pack, downloaded" else "Telangana Nature Pack, not downloaded yet", style = MaterialTheme.typography.headlineSmall)
                    Label(if (pack) "Airplane mode ready" else "Tap to get it before you head out", color = MaterialTheme.colorScheme.secondary)
                }
            }
            last?.let { t ->
                Spacer(Modifier.height(16.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(22.dp)) {
                        Label("Last trail")
                        Text("${fmtDist(t.distanceM)} in ${fmtDur((t.endMs ?: t.startMs) - t.startMs)}", style = MaterialTheme.typography.headlineSmall)
                        Label("${t.challengesDone} challenges done")
                    }
                }
            }
            Spacer(Modifier.height(36.dp))
            Button(if (pack) onStart else onPack, Modifier.fillMaxWidth().heightIn(min = 84.dp), shape = RoundedCornerShape(28.dp)) {
                Text("Start Trail", style = MaterialTheme.typography.headlineSmall)
            }
            TextButton(onNotes, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Field Notes") }
        }
    }
}

@Composable fun PackScreen(onBack: () -> Unit, vm: PackViewModel = hiltViewModel()) {
    val p by vm.progress.collectAsState(); val ready by vm.ready.collectAsState()
    PaperScreen {
        Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
            Label("Nature pack"); Text("Telangana Nature Pack", style = MaterialTheme.typography.displaySmall)
            Text("15 species · 20 challenges · about 42 MB", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(28.dp))
            if (ready) {
                Text("Airplane mode ready", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text("Everything you need is on this phone. Switch the signal off.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                Button(onBack, Modifier.fillMaxWidth().heightIn(min = 64.dp), shape = RoundedCornerShape(24.dp)) { Text("Back") }
            } else {
                LinearProgressIndicator(progress = { p }, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
                Spacer(Modifier.height(8.dp)); Label("${(p * 100).toInt()}%")
                Spacer(Modifier.height(24.dp))
                Button(vm::start, Modifier.fillMaxWidth().heightIn(min = 64.dp), enabled = p == 0f, shape = RoundedCornerShape(24.dp)) {
                    Text(if (p == 0f) "Download" else "Downloading…")
                }
                Spacer(Modifier.height(12.dp)); Label("Phase 1 simulates this step. The data is already bundled.")
            }
        }
    }
}
