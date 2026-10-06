package app.vanam.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import app.vanam.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Paper = Color(0xFFF4EEE1); private val Forest = Color(0xFF2F5233); private val Terracotta = Color(0xFFC0623B)
private val Charcoal = Color(0xFF2B2A28); private val CardC = Color(0xFFFBF7EE)
private val DuskBg = Color(0xFF14181A); private val DuskCard = Color(0xFF1E2427); private val Moss = Color(0xFF8FB58A)
private val Ember = Color(0xFFD9895F); private val DuskText = Color(0xFFD9D4C7)

private val Light = lightColorScheme(primary = Forest, onPrimary = Paper, secondary = Terracotta, background = Paper,
    onBackground = Charcoal, surface = CardC, onSurface = Charcoal, surfaceVariant = Color(0xFFE8DFCB), outline = Color(0xFF8A8171))
private val Dusk = darkColorScheme(primary = Moss, onPrimary = DuskBg, secondary = Ember, background = DuskBg,
    onBackground = DuskText, surface = DuskCard, onSurface = DuskText, surfaceVariant = Color(0xFF2A3236), outline = Color(0xFF6E7A70))

@OptIn(ExperimentalTextApi::class)
private fun vf(res: Int, w: Int) = Font(res, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
private val Fraunces = FontFamily(vf(R.font.fraunces, 400), vf(R.font.fraunces, 500))
private val Inter = FontFamily(vf(R.font.inter, 400), vf(R.font.inter, 600))
val Typo = Typography(
    displaySmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 36.sp, lineHeight = 42.sp),
    headlineSmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 24.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontSize = 17.sp, lineHeight = 26.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 1.2.sp))

@Composable fun VanamTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = if (dark) Dusk else Light, typography = Typo, content = content)
