package dev.ioslook.transformation.engine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class OperationExecution(
    val success: Boolean,
    val message: String,
    val rolledBack: Boolean = false
)

/**
 * Serializes customization operations and enforces:
 * preflight -> verified backup -> apply -> verify -> commit.
 * A failure after a verified backup triggers the operation's rollback method.
 */
class CustomizationCoordinator(
    private val journal: OperationJournal
) {
    private val mutex = Mutex()

    suspend fun execute(operation: CustomizationOperation): OperationExecution =
        mutex.withLock {
            var backup: BackupReceipt? = null

            suspend fun record(state: OperationState, detail: String) {
                journal.record(
                    OperationRecord(
                        operationId = operation.id,
                        title = operation.title,
                        state = state,
                        detail = detail,
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            }

            suspend fun failAndRollback(detail: String): OperationExecution {
                val savedBackup = backup
                if (savedBackup == null || !savedBackup.verified) {
                    record(OperationState.FAILED, detail)
                    return OperationExecution(false, detail)
                }

                record(OperationState.ROLLING_BACK, detail)
                val rollbackResult = try {
                    operation.rollback(savedBackup)
                } catch (error: Exception) {
                    OperationResult(false, error.message ?: "Rollback threw an exception")
                }

                return if (rollbackResult.success) {
                    record(OperationState.ROLLED_BACK, "$detail; rollback: ${rollbackResult.message}")
                    OperationExecution(false, "$detail; changes were rolled back", rolledBack = true)
                } else {
                    record(OperationState.FAILED, "$detail; rollback failed: ${rollbackResult.message}")
                    OperationExecution(false, "$detail; rollback failed: ${rollbackResult.message}")
                }
            }

            try {
                record(OperationState.PLANNED, "Operation queued")
                record(OperationState.CHECKING, "Checking device and operation preconditions")
                val check = operation.check()
                if (!check.allowed) {
                    record(OperationState.FAILED, check.reason)
                    return@withLock OperationExecution(false, check.reason)
                }

                record(OperationState.BACKING_UP, "Creating backup before modification")
                backup = operation.createBackup()
                if (backup?.verified != true) {
                    val reason = "Backup was not verified; modification was blocked"
                    record(OperationState.FAILED, reason)
                    return@withLock OperationExecution(false, reason)
                }

                record(OperationState.APPLYING, "Applying the requested customization")
                val applied = operation.apply()
                if (!applied.success) {
                    return@withLock failAndRollback(applied.message)
                }

                record(OperationState.VERIFYING, "Checking the resulting device state")
                val verified = operation.verify()
                if (!verified.success) {
                    return@withLock failAndRollback(verified.message)
                }

                record(OperationState.APPLIED, verified.message)
                OperationExecution(true, verified.message)
            } catch (cancelled: CancellationException) {
                try {
                    failAndRollback("Operation was cancelled")
                } catch (_: Exception) {
                    // Preserve the original coroutine cancellation.
                }
                throw cancelled
            } catch (error: Exception) {
                val reason = error.message ?: error::class.java.simpleName
                try {
                    failAndRollback("Operation failed: $reason")
                } catch (journalError: Exception) {
                    OperationExecution(
                        false,
                        "Operation failed: $reason; journal/rollback error: ${journalError.message}"
                    )
                }
            }
        }
}
