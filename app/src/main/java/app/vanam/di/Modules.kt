package app.vanam.di
import android.content.Context
import androidx.room.Room
import app.vanam.data.VanamDao
import app.vanam.data.VanamDb
import app.vanam.domain.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class) object DbModule {
    @Provides @Singleton fun db(@ApplicationContext c: Context): VanamDb = Room.databaseBuilder(c, VanamDb::class.java, "vanam.db").build()
    @Provides fun dao(db: VanamDb): VanamDao = db.dao()
}
/** Phase 2: change these three lines to swap in BirdNET, the TFLite classifier and the Gemma writer. */
@Module @InstallIn(SingletonComponent::class) abstract class ModelModule {
    @Binds abstract fun img(i: TfliteImageIdentifier): ImageIdentifier
    @Binds abstract fun aud(i: BirdNetAudioIdentifier): AudioIdentifier
    @Binds abstract fun writer(i: GemmaFieldWriter): FieldWriter
}
