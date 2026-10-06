package app.vanam
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import app.vanam.ui.*
import app.vanam.ui.theme.VanamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge(); setContent { VanamTheme { VanamNav() } } }
}
@Composable fun VanamNav() {
    val nav = rememberNavController()
    NavHost(nav, "home", enterTransition = { fadeIn(tween(500)) }, exitTransition = { fadeOut(tween(400)) },
        popEnterTransition = { fadeIn(tween(500)) }, popExitTransition = { fadeOut(tween(400)) }) {
        composable("home") { HomeScreen(onStart = { nav.navigate("trail") }, onPack = { nav.navigate("pack") }, onNotes = { nav.navigate("notes") }, onDebug = { nav.navigate("debug") }) }
        composable("pack") { PackScreen(onBack = { nav.popBackStack() }) }
        composable("trail") { TrailScreen(onFinished = { id -> nav.navigate("summary/$id") { popUpTo("home") } }) }
        composable("summary/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            SummaryScreen(onHome = { nav.popBackStack("home", false) }, onNotes = { nav.navigate("notes") })
        }
        composable("notes") { NotesScreen(onBack = { nav.popBackStack() }) }
        composable("debug") { DebugScreen(onBack = { nav.popBackStack() }) }
    }
}
