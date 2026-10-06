package app.vanam.ui
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vanam.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel class HomeViewModel @Inject constructor(dao: VanamDao, repo: Repo) : ViewModel() {
    val last = dao.trails().map { it.firstOrNull() }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val pack = repo.packReady
}
@HiltViewModel class PackViewModel @Inject constructor(private val repo: Repo) : ViewModel() {
    val progress = MutableStateFlow(0f); val ready = repo.packReady
    /** Phase 1 simulates the download: the data is already bundled. The real downloader is isolated and comes later. */
    fun start() { if (progress.value > 0f) return
        viewModelScope.launch { for (i in 1..100) { delay(45); progress.value = i / 100f }; repo.setPack(true) } }
}
data class Summary(val species: Int, val birds: Int, val plants: Int, val meters: Float, val challenges: Int, val durMs: Long, val savedMs: Long)
@HiltViewModel class SummaryViewModel @Inject constructor(h: SavedStateHandle, dao: VanamDao) : ViewModel() {
    val state = MutableStateFlow<Summary?>(null)
    init { viewModelScope.launch {
        val t = dao.trail(h.get<Long>("id") ?: 0L) ?: return@launch
        val sp = dao.speciesAll().associateBy { it.id }
        val ids = dao.discoveriesOnce(t.id).map { it.speciesId }.toSet()
        val dur = (t.endMs ?: t.startMs) - t.startMs
        state.value = Summary(ids.size, ids.count { sp[it]?.type == "bird" }, ids.count { sp[it]?.type == "plant" }, t.distanceM,
            t.challengesDone, dur, (dur - t.foregroundMs).coerceAtLeast(0))
    } }
}
data class NoteItem(val d: DiscoveryEntity, val sp: SpeciesEntity?)
data class NoteTrail(val t: TrailEntity, val items: List<NoteItem>)
@HiltViewModel class NotesViewModel @Inject constructor(dao: VanamDao) : ViewModel() {
    val state = combine(dao.trails(), dao.allDiscoveries(), dao.speciesFlow()) { ts, ds, sp ->
        val m = sp.associateBy { it.id }
        ts.map { t -> NoteTrail(t, ds.filter { it.trailId == t.id }.map { NoteItem(it, m[it.speciesId]) }) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
