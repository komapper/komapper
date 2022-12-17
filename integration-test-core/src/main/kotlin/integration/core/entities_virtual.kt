package integration.core

import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId

@KomapperEntity
data class Belonging(
    @KomapperId(virtual = true) val employeeId: Int,
    @KomapperId(virtual = true) val departmentId: Int
)

@KomapperEntity
data class Assignment(
    @KomapperEmbeddedId(virtual = true) val id: AssignmentId
)

data class AssignmentId(
    val employeeId: Int,
    val taskId: Int
)
