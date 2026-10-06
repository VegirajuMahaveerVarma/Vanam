package app.vanam.ui
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

fun fmtDur(ms: Long): String { val m = ms / 60000; return if (m >= 60) "${m / 60}h ${"%02d".format(m % 60)}m" else "${m}m ${"%02d".format(ms / 1000 % 60)}s" }
fun fmtDist(m: Float): String = if (m < 1000f) "${m.toInt()} m" else "%.2f km".format(m / 1000f)
fun Context.findActivity(): Activity? { var c = this; while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }; return null }

@Composable fun PaperScreen(content: @Composable BoxScope.() -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).drawBehind {
        val r = Random(7); repeat(260) { drawCircle(ink.copy(alpha = 0.035f), 1.2f, Offset(r.nextFloat() * size.width, r.nextFloat() * size.height)) }
    }.systemBarsPadding(), content = content)
}
@Composable fun Label(t: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outline) =
    Text(t.uppercase(), modifier, color = color, style = MaterialTheme.typography.labelSmall)

@Composable fun SketchDivider(mod: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    Canvas(mod.fillMaxWidth().height(10.dp)) {
        val p = Path(); val mid = size.height / 2; p.moveTo(0f, mid); var x = 0f; var up = true
        while (x < size.width) { p.quadraticBezierTo(x + 18f, mid + if (up) -3f else 3f, x + 36f, mid); x += 36f; up = !up }
        drawPath(p, c, style = Stroke(2f, cap = StrokeCap.Round))
    }
}
@Composable fun LeafArt(mod: Modifier, color: Color) = Canvas(mod) {
    val w = size.width; val h = size.height
    val p = Path().apply { moveTo(w * .1f, h * .9f); cubicTo(0f, h * .3f, w * .6f, 0f, w * .95f, h * .05f); cubicTo(w, h * .6f, w * .5f, h, w * .1f, h * .9f); close() }
    drawPath(p, color, style = Stroke(3f, cap = StrokeCap.Round)); drawLine(color, Offset(w * .1f, h * .9f), Offset(w * .8f, h * .2f), 3f)
}
/** A waveform that breathes when idle and swells with the real mic level. */
@Composable fun Waveform(amp: Float, active: Boolean, modifier: Modifier, color: Color) {
    val ph by rememberInfiniteTransition(label = "w").animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "p")
    val a by animateFloatAsState(if (active) amp else 0f, spring(stiffness = Spring.StiffnessLow), label = "a")
    Canvas(modifier) {
        val n = 28; val gap = size.width / n
        for (i in 0 until n) {
            val k = 0.18f + 0.1f * sin(ph + i * 0.5f) + a * 1.8f * (0.5f + 0.5f * sin(ph * 2 + i * 0.8f))
            val h = (size.height * k).coerceIn(size.height * 0.05f, size.height)
            drawLine(color, Offset(gap * (i + .5f), (size.height - h) / 2), Offset(gap * (i + .5f), (size.height + h) / 2), gap * 0.4f, StrokeCap.Round)
        }
    }
}
@Composable fun rememberPerm(permission: String, onGranted: () -> Unit): () -> Unit {
    val ctx = LocalContext.current; val cb by rememberUpdatedState(onGranted)
    val l = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) cb() }
    return { if (ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED) cb() else l.launch(permission) }
}
/** Dims the real screen to near-black while pocket mode is on. */
@Composable fun PocketEffect(on: Boolean) {
    val ctx = LocalContext.current
    DisposableEffect(on) {
        val w = ctx.findActivity()?.window
        if (on) { w?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); w?.attributes = w?.attributes?.also { it.screenBrightness = 0.01f } }
        onDispose { w?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); w?.attributes = w?.attributes?.also { it.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE } }
    }
}
