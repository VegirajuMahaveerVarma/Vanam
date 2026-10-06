package app.vanam.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity data class SpeciesEntity(@PrimaryKey val id: String, val common: String, val telugu: String?, val scientific: String,
    val type: String, val fieldMarks: String, val habitat: String, val season: String, val confirmCue: String, val art: String?)
@Entity data class TrailEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val startMs: Long, val endMs: Long?,
    val distanceM: Float, val foregroundMs: Long, val challengesDone: Int)
@Entity data class DiscoveryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val trailId: Long, val speciesId: String,
    val confidence: Float, val lat: Double?, val lon: Double?, val photoPath: String?, val note: String, val atMs: Long)
@Entity data class ChallengeEntity(@PrimaryKey val id: Int, val text: String, val kind: String, val timeOfDay: String, val season: String)

@Dao interface VanamDao {
    @Query("SELECT * FROM SpeciesEntity WHERE id = :id") suspend fun species(id: String): SpeciesEntity?
    @Query("SELECT COUNT(*) FROM SpeciesEntity") suspend fun speciesCount(): Int
    @Query("SELECT * FROM SpeciesEntity") fun speciesFlow(): Flow<List<SpeciesEntity>>
    @Query("SELECT * FROM SpeciesEntity") suspend fun speciesAll(): List<SpeciesEntity>
    @Query("SELECT * FROM ChallengeEntity") suspend fun challenges(): List<ChallengeEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSpecies(s: List<SpeciesEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertChallenges(c: List<ChallengeEntity>)
    @Insert suspend fun insertTrail(t: TrailEntity): Long
    @Update suspend fun updateTrail(t: TrailEntity)
    @Insert suspend fun insertDiscovery(d: DiscoveryEntity)
    @Query("SELECT * FROM TrailEntity WHERE id = :id") suspend fun trail(id: Long): TrailEntity?
    @Query("SELECT * FROM DiscoveryEntity WHERE trailId = :t") suspend fun discoveriesOnce(t: Long): List<DiscoveryEntity>
    @Query("SELECT * FROM TrailEntity WHERE endMs IS NOT NULL ORDER BY startMs DESC") fun trails(): Flow<List<TrailEntity>>
    @Query("SELECT * FROM DiscoveryEntity ORDER BY atMs") fun allDiscoveries(): Flow<List<DiscoveryEntity>>
}
@Database(entities = [SpeciesEntity::class, TrailEntity::class, DiscoveryEntity::class, ChallengeEntity::class], version = 1, exportSchema = false)
abstract class VanamDb : RoomDatabase() { abstract fun dao(): VanamDao }
