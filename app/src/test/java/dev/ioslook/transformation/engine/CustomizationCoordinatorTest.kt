package dev.ioslook.transformation.engine

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomizationCoordinatorTest {

    @Test
    fun successfulOperationIsAppliedAndVerified() = runBlocking {
        val journal = InMemoryOperationJournal()
        val operation = FakeOperation()
        val result = CustomizationCoordinator(journal).execute(operation)

        assertTrue(result.success)
        assertFalse(result.rolledBack)
        assertEquals(1, operation.applyCalls)
        assertEquals(0, operation.rollbackCalls)
        assertEquals(OperationState.APPLIED, journal.events.last().state)
    }

    @Test
    fun failedVerificationAutomaticallyRollsBack() = runBlocking {
        val journal = InMemoryOperationJournal()
        val operation = FakeOperation(verifySuccess = false)
        val result = CustomizationCoordinator(journal).execute(operation)

        assertFalse(result.success)
        assertTrue(result.rolledBack)
        assertEquals(1, operation.rollbackCalls)
        assertEquals(OperationState.ROLLED_BACK, journal.events.last().state)
    }

    @Test
    fun unverifiedBackupBlocksAllModification() = runBlocking {
        val journal = InMemoryOperationJournal()
        val operation = FakeOperation(backupVerified = false)
        val result = CustomizationCoordinator(journal).execute(operation)

        assertFalse(result.success)
        assertEquals(0, operation.applyCalls)
        assertEquals(0, operation.rollbackCalls)
        assertEquals(OperationState.FAILED, journal.events.last().state)
    }

    @Test
    fun failedApplyTriggersRollback() = runBlocking {
        val journal = InMemoryOperationJournal()
        val operation = FakeOperation(applySuccess = false)
        val result = CustomizationCoordinator(journal).execute(operation)

        assertFalse(result.success)
        assertTrue(result.rolledBack)
        assertEquals(1, operation.rollbackCalls)
        assertEquals(OperationState.ROLLED_BACK, journal.events.last().state)
    }

    private class InMemoryOperationJournal : OperationJournal {
        val events = mutableListOf<OperationRecord>()

        override suspend fun record(record: OperationRecord) {
            events += record
        }

        override suspend fun latest(): List<OperationRecord> = events.asReversed()
    }

    private class FakeOperation(
        private val backupVerified: Boolean = true,
        private val applySuccess: Boolean = true,
        private val verifySuccess: Boolean = true
    ) : CustomizationOperation {
        override val id = "test-operation"
        override val title = "Test operation"
        override val risk = OperationRisk.LOW

        var applyCalls = 0
        var rollbackCalls = 0

        override suspend fun check() = OperationCheck(allowed = true, reason = "Preconditions passed")

        override suspend fun createBackup() = BackupReceipt(
            id = "backup-test",
            description = "Test backup",
            verified = backupVerified
        )

        override suspend fun apply(): OperationResult {
            applyCalls += 1
            return OperationResult(applySuccess, if (applySuccess) "Applied" else "Apply failed")
        }

        override suspend fun verify() = OperationResult(
            verifySuccess,
            if (verifySuccess) "Verification passed" else "Verification failed"
        )

        override suspend fun rollback(backup: BackupReceipt): OperationResult {
            rollbackCalls += 1
            return OperationResult(true, "Rollback completed")
        }
    }
}
