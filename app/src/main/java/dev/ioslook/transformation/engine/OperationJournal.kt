package dev.ioslook.transformation.engine

enum class OperationState {
    PLANNED,
    CHECKING,
    BACKING_UP,
    APPLYING,
    VERIFYING,
    APPLIED,
    FAILED,
    ROLLING_BACK,
    ROLLED_BACK
}

data class OperationRecord(
    val operationId: String,
    val title: String,
    val state: OperationState,
    val detail: String,
    val updatedAtEpochMillis: Long
)

/**
 * Durable state-transition boundary. The current implementation stores a
 * bounded journal in app-private SharedPreferences; the interface allows a
 * future migration to Room without changing the coordinator.
 */
interface OperationJournal {
    suspend fun record(record: OperationRecord)
    suspend fun latest(): List<OperationRecord>
}
