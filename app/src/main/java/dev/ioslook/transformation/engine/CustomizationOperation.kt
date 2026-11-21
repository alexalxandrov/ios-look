package dev.ioslook.transformation.engine

enum class CapabilityStatus {
    SUPPORTED,
    PARTIAL,
    UNAVAILABLE,
    UNTESTED
}

enum class OperationRisk {
    LOW,
    MODERATE,
    HIGH
}

data class CapabilityReport(
    val id: String,
    val title: String,
    val status: CapabilityStatus,
    val explanation: String,
    val requiresRoot: Boolean = false
)

data class OperationCheck(
    val allowed: Boolean,
    val reason: String,
    val requiresRoot: Boolean = false
)

data class BackupReceipt(
    val id: String,
    val description: String,
    val verified: Boolean
)

data class OperationResult(
    val success: Boolean,
    val message: String
)

/**
 * Contract for every future customization action.
 *
 * An operation must provide a rollback implementation before it can be
 * registered with the coordinator. Implementations must be narrowly scoped
 * and must not perform work from constructors or capability probes.
 */
interface CustomizationOperation {
    val id: String
    val title: String
    val risk: OperationRisk

    suspend fun check(): OperationCheck
    suspend fun createBackup(): BackupReceipt
    suspend fun apply(): OperationResult
    suspend fun verify(): OperationResult
    suspend fun rollback(backup: BackupReceipt): OperationResult
}
