package dev.ioslook.transformation.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Durable local journal backed by app-private SharedPreferences.
 * Writes are committed on an I/O dispatcher before the coordinator advances.
 */
class PreferencesOperationJournal(context: Context) : OperationJournal {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override suspend fun record(record: OperationRecord) {
        withContext(Dispatchers.IO) {
            synchronized(lock) {
                val current = JSONArray(preferences.getString(KEY_RECORDS, "[]") ?: "[]")
                current.put(record.toJson())
                val retained = JSONArray()
                val firstIndex = (current.length() - MAX_RECORDS).coerceAtLeast(0)
                for (index in firstIndex until current.length()) {
                    retained.put(current.getJSONObject(index))
                }
                if (!preferences.edit().putString(KEY_RECORDS, retained.toString()).commit()) {
                    throw IOException("Operation journal could not be committed to local storage")
                }
            }
        }
    }

    override suspend fun latest(): List<OperationRecord> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val stored = JSONArray(preferences.getString(KEY_RECORDS, "[]") ?: "[]")
            buildList {
                for (index in stored.length() - 1 downTo 0) {
                    add(stored.getJSONObject(index).toOperationRecord())
                }
            }
        }
    }

    private fun OperationRecord.toJson(): JSONObject = JSONObject()
        .put("operationId", operationId)
        .put("title", title)
        .put("state", state.name)
        .put("detail", detail)
        .put("updatedAtEpochMillis", updatedAtEpochMillis)

    private fun JSONObject.toOperationRecord(): OperationRecord = OperationRecord(
        operationId = getString("operationId"),
        title = getString("title"),
        state = OperationState.valueOf(getString("state")),
        detail = getString("detail"),
        updatedAtEpochMillis = getLong("updatedAtEpochMillis")
    )

    private companion object {
        const val PREFERENCES_NAME = "ios_transformation_operation_journal"
        const val KEY_RECORDS = "records"
        const val MAX_RECORDS = 250
        val lock = Any()
    }
}
