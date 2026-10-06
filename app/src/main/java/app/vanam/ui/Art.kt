package app.vanam.ui
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable fun BinocularsArt(mod: Modifier, color: Color) = Canvas(mod) {
    val w = size.width; val h = size.height; val r = h * 0.3f; val st = Stroke(3f, cap = StrokeCap.Round)
    val lx = w * 0.27f; val rx = w * 0.73f; val cy = h * 0.64f
    drawCircle(color, r, Offset(lx, cy), style = st); drawCircle(color, r, Offset(rx, cy), style = st)
    drawCircle(color, r * 0.55f, Offset(lx, cy), style = st); drawCircle(color, r * 0.55f, Offset(rx, cy), style = st)
    drawLine(color, Offset(lx + r, cy), Offset(rx - r, cy), 3f, StrokeCap.Round)
    drawLine(color, Offset(lx - r * 0.6f, cy - r * 0.8f), Offset(lx - r * 0.45f, h * 0.08f), 3f, StrokeCap.Round)
    drawLine(color, Offset(rx + r * 0.6f, cy - r * 0.8f), Offset(rx + r * 0.45f, h * 0.08f), 3f, StrokeCap.Round)
    drawLine(color, Offset(lx - r * 0.45f, h * 0.08f), Offset(lx + r * 0.2f, h * 0.08f), 3f, StrokeCap.Round)
    drawLine(color, Offset(rx + r * 0.45f, h * 0.08f), Offset(rx - r * 0.2f, h * 0.08f), 3f, StrokeCap.Round)
}
@Composable fun BirdArt(mod: Modifier, color: Color) = Canvas(mod) {
    val w = size.width; val h = size.height; val st = Stroke(3f, cap = StrokeCap.Round)
    drawCircle(color, h * 0.26f, Offset(w * 0.45f, h * 0.52f), style = st)
    drawCircle(color, h * 0.13f, Offset(w * 0.74f, h * 0.3f), style = st)
    drawCircle(color, 3f, Offset(w * 0.77f, h * 0.28f))
    drawLine(color, Offset(w * 0.74f + h * 0.13f, h * 0.29f), Offset(w * 0.97f, h * 0.35f), 3f, StrokeCap.Round)
    drawLine(color, Offset(w * 0.74f + h * 0.13f, h * 0.35f), Offset(w * 0.97f, h * 0.35f), 3f, StrokeCap.Round)
    drawLine(color, Offset(w * 0.22f, h * 0.55f), Offset(w * 0.02f, h * 0.82f), 3f, StrokeCap.Round)
    drawLine(color, Offset(w * 0.24f, h * 0.62f), Offset(w * 0.07f, h * 0.9f), 3f, StrokeCap.Round)
    drawLine(color, Offset(w * 0.45f, h * 0.78f), Offset(w * 0.45f, h * 0.93f), 3f, StrokeCap.Round)
    drawLine(color, Offset(w * 0.15f, h * 0.95f), Offset(w * 0.85f, h * 0.95f), 3f, StrokeCap.Round)
}
/** A simple offline "map": saved sighting coordinates plotted on a paper grid. No tiles, no network. */
@Composable fun MiniMap(points: List<Pair<Double, Double>>, mod: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.outline; val dot = MaterialTheme.colorScheme.secondary
    Canvas(mod.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        var gx = 0f; while (gx < size.width) { drawLine(ink.copy(alpha = 0.18f), Offset(gx, 0f), Offset(gx, size.height), 1f); gx += 40f }
        var gy = 0f; while (gy < size.height) { drawLine(ink.copy(alpha = 0.18f), Offset(0f, gy), Offset(size.width, gy), 1f); gy += 40f }
        val minLat = points.minOf { it.first }; val maxLat = points.maxOf { it.first }
        val minLon = points.minOf { it.second }; val maxLon = points.maxOf { it.second }
        val sLat = maxOf(maxLat - minLat, 0.0005); val sLon = maxOf(maxLon - minLon, 0.0005); val pad = 28f
        points.forEach { (la, lo) ->
            val c = Offset(pad + ((lo - minLon) / sLon * (size.width - 2 * pad)).toFloat(), pad + ((maxLat - la) / sLat * (size.height - 2 * pad)).toFloat())
            drawCircle(dot, 6f, c); drawCircle(ink, 12f, c, style = Stroke(2f))
        }
        drawLine(ink, Offset(size.width - 24f, 34f), Offset(size.width - 24f, 12f), 3f, StrokeCap.Round)
        drawLine(ink, Offset(size.width - 24f, 12f), Offset(size.width - 30f, 20f), 3f, StrokeCap.Round)
        drawLine(ink, Offset(size.width - 24f, 12f), Offset(size.width - 18f, 20f), 3f, StrokeCap.Round)
    }
}
