package app.vanam.data
import android.content.Context
import app.vanam.domain.BirdLabels
import app.vanam.domain.SpeciesFacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class Repo @Inject constructor(private val dao: VanamDao, @ApplicationContext private val ctx: Context) {
    private val prefs = ctx.getSharedPreferences("vanam", Context.MODE_PRIVATE)
    private val lock = Mutex()
    private val _pack = MutableStateFlow(prefs.getBoolean("pack", false))
    val packReady: StateFlow<Boolean> = _pack
    fun setPack(v: Boolean) { prefs.edit().putBoolean("pack", v).apply(); _pack.value = v }
    suspend fun ensureSeeded() = lock.withLock {
        if (dao.speciesCount() == 0) { dao.insertSpecies(Seed.species); dao.insertChallenges(Seed.challenges) }
    }
    /** Seeded species come from the DB. Any other BirdNET bird gets a names-only entry (no invented field marks) and is stored. */
    suspend fun facts(id: String): SpeciesFacts? {
        val e = dao.species(id) ?: withContext(Dispatchers.Default) { BirdLabels.find(ctx, id) }?.let { l ->
            SpeciesEntity(id, l.common, null, l.scientific, "bird", "", "", "", "Stay still and look toward where the sound came from.", null)
                .also { dao.insertSpecies(listOf(it)) }
        } ?: return null
        return SpeciesFacts(e.id, e.common, e.telugu, e.scientific, e.type, e.fieldMarks, e.habitat, e.season, e.confirmCue, e.art)
    }
}
