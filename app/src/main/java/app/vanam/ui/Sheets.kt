@file:OptIn(ExperimentalMaterial3Api::class)
package app.vanam.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.vanam.domain.ConfidencePolicy
import app.vanam.domain.Verdict
import kotlinx.coroutines.delay

@Composable fun ResultSheet(s: Sighting, onSave: () -> Unit, onDismiss: () -> Unit) {
    LaunchedEffect(s) { delay(15000); onDismiss() }
    val f = s.facts; val ok = f != null && s.verdict !is Verdict.NotSure
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 28.dp).padding(bottom = 32.dp)) {
            Label(if (s.via == "listen") "Heard" else "Seen")
            Spacer(Modifier.height(8.dp))
            Text(if (!ok) "Not sure" else if (s.verdict is Verdict.Probably) "Probably ${f!!.common}" else f!!.common, style = MaterialTheme.typography.displaySmall)
            if (ok && f!!.telugu != null) Text(f.telugu!!, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(14.dp))
            Label(ConfidencePolicy.word(s.conf))
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(progress = { s.conf }, Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(18.dp))
            Text(if (ok) (s.note?.blurb ?: "") else if (s.via == "listen") "Too faint to say. Stay quiet and listen again." else "Too far or too blurry. Get a little closer and try again.", style = MaterialTheme.typography.bodyLarge)
            if (ok) { Spacer(Modifier.height(8.dp)); Text("To confirm: ${f!!.confirmCue}", style = MaterialTheme.typography.bodyLarge) }
            Spacer(Modifier.height(16.dp))
            Text(if (ok) "Put your phone away and look for it." else "Or just keep walking. It will come again.", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            if (ok) { Spacer(Modifier.height(20.dp)); Button(onSave, Modifier.fillMaxWidth().heightIn(min = 64.dp), shape = RoundedCornerShape(24.dp)) { Text("Save to Field Notes") } }
        }
    }
}
@Composable fun ChallengeSheet(text: String, onDone: () -> Unit, onSwap: () -> Unit, onSkip: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onSkip, sheetState = rememberModalBottomSheetState(true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 28.dp).padding(bottom = 32.dp)) {
            Label("A tiny challenge"); Spacer(Modifier.height(10.dp))
            Text(text, style = MaterialTheme.typography.displaySmall); Spacer(Modifier.height(10.dp))
            Text("Then put the phone away.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(22.dp))
            Button(onDone, Modifier.fillMaxWidth().heightIn(min = 64.dp), shape = RoundedCornerShape(24.dp)) { Text("Done") }
            Row(Modifier.fillMaxWidth()) {
                TextButton(onSwap, Modifier.weight(1f).heightIn(min = 56.dp)) { Text("Swap") }
                TextButton(onSkip, Modifier.weight(1f).heightIn(min = 56.dp)) { Text("Skip") }
            }
        }
    }
}
