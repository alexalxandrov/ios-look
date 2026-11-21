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
 * Persistence boundary. The production implementation will use Room.
 * State transitions must be written before and after each mutating step.
 */
interface OperationJournal {
    suspend fun record(record: OperationRecord)
    suspend fun latest(): List<OperationRecord>
}
