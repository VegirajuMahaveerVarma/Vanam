@file:OptIn(ExperimentalMaterial3Api::class)
package app.vanam.ui
import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.io.File

@Composable fun TrailScreen(onFinished: (Long) -> Unit, vm: TrailViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState(); val haptic = LocalHapticFeedback.current; val owner = LocalLifecycleOwner.current
    val askLoc = rememberPerm(Manifest.permission.ACCESS_FINE_LOCATION) { vm.startLocation() }
    val askCam = rememberPerm(Manifest.permission.CAMERA) { vm.setCamera(true) }
    val askMic = rememberPerm(Manifest.permission.RECORD_AUDIO) { vm.listen() }
    LaunchedEffect(Unit) { askLoc() }
    LaunchedEffect(ui.buzz) { if (ui.buzz > 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    LaunchedEffect(ui.finishedId) { ui.finishedId?.let(onFinished) }
    DisposableEffect(owner) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) vm.setForeground(true) else if (e == Lifecycle.Event.ON_PAUSE) vm.setForeground(false) }
        owner.lifecycle.addObserver(o); onDispose { owner.lifecycle.removeObserver(o) }
    }
    PocketEffect(ui.pocket)
    var confirm by remember { mutableStateOf(false) }
    BackHandler { if (!ui.pocket) confirm = true }
    Box(Modifier.fillMaxSize()) {
        PaperScreen {
            Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Label("${fmtDur(ui.elapsedMs)} · ${fmtDist(ui.distanceM)}"); Spacer(Modifier.weight(1f)); Label("${ui.found} found")
                }
                Text(if (ui.busy) "Thinking quietly…" else "Listen. Then look up.", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                ActionCard("LOOK", "Point, snap, then lower the phone", Icons.Filled.CameraAlt, Modifier.weight(1f), art = { BinocularsArt(Modifier.size(44.dp), MaterialTheme.colorScheme.primary) }) { if (!ui.busy) askCam() }
                ActionCard("LISTEN", if (ui.listening) "Listening…" else "Five quiet seconds", Icons.Filled.Mic, Modifier.weight(1f),
                    extra = { if (ui.listening) Waveform(ui.amp, true, Modifier.fillMaxWidth().height(44.dp), MaterialTheme.colorScheme.secondary) }) { if (!ui.busy) askMic() }
                ActionCard("EXPLORE", "One tiny outdoor challenge", Icons.Filled.Explore, Modifier.weight(1f)) { vm.explore() }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton({ vm.setPocket(true) }, Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(20.dp)) { Text("Pocket mode") }
                    Spacer(Modifier.width(12.dp))
                    TextButton({ confirm = true }, Modifier.heightIn(min = 56.dp)) { Text("Finish trail") }
                }
            }
            if (ui.camera) CameraCapture({ vm.onPhoto(it) }, { vm.setCamera(false) })
        }
        if (ui.pocket) Box(Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) { detectTapGestures(onLongPress = { vm.setPocket(false) }) }
            .semantics { contentDescription = "Pocket mode. Press and hold to wake the screen." }, contentAlignment = Alignment.Center) {
            Text("Put me away. I'll be here.", color = Color(0xFF3C3C3C), style = MaterialTheme.typography.headlineSmall)
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("End this trail?") },
        text = { Text("Everything you saved stays in Field Notes.") },
        confirmButton = { TextButton({ confirm = false; vm.finish() }) { Text("End trail") } },
        dismissButton = { TextButton({ confirm = false }) { Text("Keep walking") } })
    ui.sighting?.let { ResultSheet(it, vm::save, vm::dismissSighting) }
    ui.challenge?.let { ChallengeSheet(it.text, vm::completeChallenge, vm::explore, vm::skipChallenge) }
}

@Composable fun ActionCard(title: String, hint: String, icon: ImageVector, modifier: Modifier, extra: @Composable () -> Unit = {}, art: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    Surface(onClick, modifier.fillMaxWidth().semantics { contentDescription = "$title. $hint" }, shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            if (art != null) art() else Icon(icon, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(20.dp))
            Column { Text(title, style = MaterialTheme.typography.headlineSmall); Text(hint, style = MaterialTheme.typography.bodyLarge); extra() }
        }
    }
}

@Composable fun CameraCapture(onCaptured: (String) -> Unit, onClose: () -> Unit) {
    val ctx = LocalContext.current; val owner = LocalLifecycleOwner.current
    val capture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val view = remember { PreviewView(ctx) }
    DisposableEffect(Unit) {
        val f = ProcessCameraProvider.getInstance(ctx)
        f.addListener({
            val p = f.get(); val prev = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
            p.unbindAll(); p.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, prev, capture)
        }, ContextCompat.getMainExecutor(ctx))
        onDispose { runCatching { f.get().unbindAll() } }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView({ view }, Modifier.fillMaxSize())
        IconButton(onClose, Modifier.align(Alignment.TopEnd).padding(12.dp).size(56.dp)) { Icon(Icons.Filled.Close, "Close camera", tint = Color.White) }
        Surface({
            val file = File(ctx.filesDir, "photo_${System.currentTimeMillis()}.jpg")
            capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(ctx),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(r: ImageCapture.OutputFileResults) = onCaptured(file.absolutePath)
                    override fun onError(e: ImageCaptureException) = onClose()
                })
        }, Modifier.align(Alignment.BottomCenter).padding(40.dp).size(84.dp).semantics { contentDescription = "Take photo" },
            shape = CircleShape, color = Color(0xFFF4EEE1), border = BorderStroke(5.dp, Color(0xFFC0623B))) {}
    }
}
