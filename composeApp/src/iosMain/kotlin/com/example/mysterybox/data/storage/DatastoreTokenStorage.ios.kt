package com.example.mysterybox.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun createDataStore(): DataStore<Preferences> {
    return createDataStore(
        produceFile = { ->
            val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask,
                null,
                true,
                null,
            )
            (documentDirectory?.path + "/token_prefs.preferences_pb")
        }
    )
}
