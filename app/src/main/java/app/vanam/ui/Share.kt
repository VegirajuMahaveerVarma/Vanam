package app.vanam.ui
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File

/** Draws the share card locally. Nothing is uploaded. */
fun shareSummary(ctx: Context, s: Summary) {
    val bmp = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp); c.drawColor(0xFFF4EEE1.toInt())
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.typeface = Typeface.SERIF; p.color = 0xFF2F5233.toInt(); p.textSize = 120f; c.drawText("Vanam", 90f, 200f, p)
    p.color = 0xFF2B2A28.toInt(); p.textSize = 46f; c.drawText("A walk, written down.", 90f, 275f, p)
    val rows = listOf("Species" to "${s.species}", "Birds / plants" to "${s.birds} / ${s.plants}", "Distance" to fmtDist(s.meters),
        "Time outside" to fmtDur(s.durMs), "Challenges" to "${s.challenges}", "Screen time saved" to fmtDur(s.savedMs))
    rows.forEachIndexed { i, (k, v) ->
        val y = 470f + i * 130f
        p.typeface = Typeface.MONOSPACE; p.textSize = 30f; p.color = 0xFF8A8171.toInt(); c.drawText(k.uppercase(), 90f, y, p)
        p.typeface = Typeface.SERIF; p.textSize = 64f; p.color = 0xFF2B2A28.toInt(); c.drawText(v, 90f, y + 68f, p)
    }
    p.typeface = Typeface.MONOSPACE; p.textSize = 28f; p.color = 0xFFC0623B.toInt(); c.drawText("AI FIELD GUIDE. NO SIGNAL REQUIRED.", 90f, 1290f, p)
    val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
    val f = File(dir, "vanam_trail.png"); f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
    val i = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    ctx.startActivity(Intent.createChooser(i, "Share trail"))
}
