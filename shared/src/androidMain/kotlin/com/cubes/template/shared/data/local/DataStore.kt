package com.cubes.template.shared.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun getDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
)

class DataStoreFactoryImpl(private val context: Context) : DataStoreFactory {
    override fun get(): DataStore<Preferences> = getDataStore(context)
}
