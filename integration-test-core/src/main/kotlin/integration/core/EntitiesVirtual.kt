package integration.core

import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId

@KomapperEntity
public data class Belonging(
    @KomapperId(virtual = true) val employeeId: Int,
    @KomapperId(virtual = true) val departmentId: Int,
)

@KomapperEntity
public data class Assignment(
    @KomapperEmbeddedId(virtual = true) val id: AssignmentId,
)

public data class AssignmentId(
    val employeeId: Int,
    val taskId: Int,
)
