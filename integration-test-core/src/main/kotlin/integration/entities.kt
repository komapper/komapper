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
    @KomapperId @KomapperColumn(name = "address_id") val addressId: Int,
    val street: String,
    @KomapperVersion val version: Int
)

@KomapperEntity
@KomapperTable(name = "comp_key_address")
data class CompositeKeyAddress(
    @KomapperId val addressId1: Int,
    @KomapperId val addressId2: Int,
    val street: String,
    val version: Int
)

@KomapperEntity
@KomapperTable("identity_strategy")
data class IdentityStrategy(
    @KomapperId @KomapperAutoIncrement val id: Int?,
    @KomapperColumn(alwaysQuote = true)val value: String
)

@KomapperEntity
@KomapperTable("sequence_strategy")
data class SequenceStrategy(
    @KomapperId @KomapperSequence(name = "sequence_strategy_id", incrementBy = 100) val id: Int,
    @KomapperColumn(alwaysQuote = true)val value: String
)

@KomapperEntity
data class Person(
    @KomapperId @KomapperColumn("person_id") val personId: Int,
    val name: String,
    @KomapperCreatedAt @KomapperColumn("created_at") val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt @KomapperColumn("updated_at") val updatedAt: LocalDateTime? = null
)

@KomapperEntity
@KomapperTable("person")
data class Human(
    @KomapperId @KomapperColumn("person_id") val humanId: Int,
    val name: String,
    @KomapperCreatedAt val createdAt: OffsetDateTime? = null,
    @KomapperUpdatedAt val updatedAt: OffsetDateTime? = null
)

@KomapperEntity(["employee", "manager"])
data class Employee(
    @KomapperId @KomapperColumn("employee_id") val employeeId: Int,
    @KomapperColumn("employee_no") val employeeNo: Int,
    @KomapperColumn("employee_name") val employeeName: String,
    @KomapperColumn("manager_id") val managerId: Int?,
    val hiredate: LocalDate,
    val salary: BigDecimal,
    @KomapperColumn("department_id") val departmentId: Int,
    @KomapperColumn("address_id") val addressId: Int,
    @KomapperVersion val version: Int,
)

@KomapperEntity
data class Department(
    @KomapperId @KomapperColumn("department_id") val departmentId: Int,
    @KomapperColumn("department_no") val departmentNo: Int,
    @KomapperColumn("department_name") val departmentName: String,
    val location: String,
    @KomapperVersion val version: Int,
)

@KomapperEntity
@KomapperTable("department")
data class NoVersionDepartment(
    @KomapperId @KomapperColumn("department_id") val departmentId: Int,
    @KomapperColumn("department_no") val departmentNo: Int,
    @KomapperColumn("department_name") val departmentName: String,
    val location: String,
    val version: Int,
)
