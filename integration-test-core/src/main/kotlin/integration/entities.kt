package integration

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@KomapperEntity
data class Address(
    @KomapperId @KomapperColumn(name = "ADDRESS_ID") val addressId: Int,
    val street: String,
    @KomapperVersion val version: Int
)

@KomapperEntity
data class CompositeKeyAddress(
    @KomapperId val addressId1: Int,
    @KomapperId val addressId2: Int,
    val street: String,
    val version: Int
)

@KomapperEntity
@KomapperTable("IDENTITY_STRATEGY")
data class IdentityStrategy(
    @KomapperId @KomapperAutoIncrement val id: Int?,
    val value: String
)

@KomapperEntity
@KomapperTable("SEQUENCE_STRATEGY")
data class SequenceStrategy(
    @KomapperId @KomapperSequence(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100) val id: Int,
    val value: String
)

@KomapperEntity
data class Person(
    @KomapperId @KomapperColumn("PERSON_ID") val personId: Int,
    val name: String,
    @KomapperCreatedAt @KomapperColumn("CREATED_AT") val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt @KomapperColumn("UPDATED_AT") val updatedAt: LocalDateTime? = null
)

@KomapperEntity
@KomapperTable("PERSON")
data class Human(
    @KomapperId @KomapperColumn("PERSON_ID") val humanId: Int,
    val name: String,
    @KomapperCreatedAt val createdAt: OffsetDateTime? = null,
    @KomapperUpdatedAt val updatedAt: OffsetDateTime? = null
)

@KomapperEntity(["employee", "manager"])
data class Employee(
    @KomapperId @KomapperColumn("EMPLOYEE_ID") val employeeId: Int,
    @KomapperColumn("EMPLOYEE_NO") val employeeNo: Int,
    @KomapperColumn("EMPLOYEE_NAME") val employeeName: String,
    @KomapperColumn("MANAGER_ID") val managerId: Int?,
    val hiredate: LocalDate,
    val salary: BigDecimal,
    @KomapperColumn("DEPARTMENT_ID") val departmentId: Int,
    @KomapperColumn("ADDRESS_ID") val addressId: Int,
    @KomapperVersion val version: Int,
)

data class WorkerSalary(val salary: BigDecimal)

data class WorkerDetail(
    val hiredate: LocalDate,
    val salary: WorkerSalary
)

data class Worker(
    val employeeId: Int,
    val employeeNo: Int,
    val employeeName: String,
    val managerId: Int?,
    val detail: WorkerDetail,
    val departmentId: Int,
    val addressId: Int,
    val version: Int
)

data class Common(
    val personId: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0),
    val updatedAt: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0),
    val version: Int = 0
)

@KomapperEntity
data class Department(
    @KomapperId @KomapperColumn("DEPARTMENT_ID") val departmentId: Int,
    @KomapperColumn("DEPARTMENT_NO") val departmentNo: Int,
    @KomapperColumn("DEPARTMENT_NAME") val departmentName: String,
    val location: String,
    @KomapperVersion val version: Int,
)

@KomapperEntity
@KomapperTable("DEPARTMENT")
data class NoVersionDepartment(
    @KomapperId @KomapperColumn("DEPARTMENT_ID") val departmentId: Int,
    @KomapperColumn("DEPARTMENT_NO") val departmentNo: Int,
    @KomapperColumn("DEPARTMENT_NAME") val departmentName: String,
    val location: String,
    val version: Int,
)

data class NoId(val value1: Int, val value2: Int)
