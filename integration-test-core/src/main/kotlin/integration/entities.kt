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
@KomapperTable(name = "COMP_KEY_ADDRESS")
data class CompositeKeyAddress(
    @KomapperId val addressId1: Int,
    @KomapperId val addressId2: Int,
    val street: String,
    val version: Int
)

@KomapperEntity
data class CompositeKey(
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
