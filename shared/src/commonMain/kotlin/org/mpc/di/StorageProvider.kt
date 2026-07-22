package org.mpc.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import org.mpc.core.createDataStore

@ContributesTo(AppScope::class)
@BindingContainer
class StorageProvider {
    @Provides
    fun provideDataStore(
        @Provides storage: Storage<Preferences>,
    ): DataStore<Preferences> = createDataStore(storage)
}
