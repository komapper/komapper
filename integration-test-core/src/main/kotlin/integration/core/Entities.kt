package integration.core

import org.komapper.annotation.KomapperAggregateRoot
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperColumnOverride
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperLink
import org.komapper.annotation.KomapperManyToOne
import org.komapper.annotation.KomapperOneToMany
import org.komapper.annotation.KomapperOneToOne
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.core.type.ClobString
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@KomapperEntity
data class Address(
    @KomapperId
    @KomapperColumn(name = "address_id")
    val addressId: Int,
    val street: String,
    @KomapperVersion val version: Int,
)

@KomapperEntity
@KomapperTable(name = "comp_key_address")
data class CompositeKeyAddress(
    @KomapperId val addressId1: Int,
    @KomapperId val addressId2: Int,
    val street: String,
    val version: Int,
)

data class AddressId(
    val addressId1: Int,
    val addressId2: Int,
)

@KomapperEntity
@KomapperTable(name = "comp_key_address")
data class EmbeddedIdAddress(
    @KomapperEmbeddedId
    val id: AddressId,
    val street: String,
    val version: Int,
)

@KomapperEntity
@KomapperTable(name = "comp_key_address")
data class GenericEmbeddedIdAddress(
    @KomapperEmbeddedId
    @KomapperColumnOverride("first", KomapperColumn("address_id1"))
    @KomapperColumnOverride("second", KomapperColumn("address_id2"))
    val id: Pair<Int, Int>,
    val street: String,
    val version: Int,
)

@KomapperEntity
@KomapperTable("identity_strategy")
data class IdentityStrategy(
    @KomapperId @KomapperAutoIncrement
    val id: Int?,
    @KomapperColumn(alwaysQuote = true) val value: String,
)

@KomapperEntity
@KomapperTable("sequence_strategy")
data class SequenceStrategy(
    @KomapperId
    @KomapperSequence(name = "sequence_strategy_id", incrementBy = 100)
    val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: String,
)

@KomapperEntity
@KomapperTable("human")
data class Man(
    @KomapperId
    @KomapperColumn("human_id")
    val manId: Int,
    val name: String,
    @KomapperCreatedAt
    @KomapperColumn("created_at")
    val createdAt: Instant? = null,
    @KomapperUpdatedAt
    @KomapperColumn("updated_at")
    val updatedAt: Instant? = null,
)

@KomapperEntity
data class Person(
    @KomapperId
    @KomapperColumn("person_id")
    val personId: Int,
    val name: String,
    @KomapperCreatedAt
    @KomapperColumn("created_at")
    val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt
    @KomapperColumn("updated_at")
    val updatedAt: LocalDateTime? = null,
)

@KomapperEntity
@KomapperTable("human")
data class Human(
    @KomapperId
    @KomapperColumn("human_id")
    val humanId: Int,
    val name: String,
    @KomapperCreatedAt val createdAt: OffsetDateTime? = null,
    @KomapperUpdatedAt val updatedAt: OffsetDateTime? = null,
)

@KomapperManyToOne(Department::class)
@KomapperOneToOne(Address::class)
@KomapperManyToOne(Employee::class, link = KomapperLink(target = "manager"))
@KomapperOneToMany(Employee::class, navigator = "employees", link = KomapperLink(source = "manager", target = "employee"))
@KomapperEntity(["employee", "manager"])
data class Employee(
    @KomapperId
    @KomapperColumn("employee_id")
    val employeeId: Int,
    @KomapperColumn("employee_no") val employeeNo: Int,
    @KomapperColumn("employee_name") val employeeName: String,
    @KomapperColumn("manager_id") val managerId: Int?,
    val hiredate: LocalDate,
    val salary: BigDecimal,
    @KomapperColumn("department_id") val departmentId: Int,
    @KomapperColumn("address_id") val addressId: Int,
    @KomapperVersion val version: Int,
)

data class RobotInfo1(
    @KomapperColumn("employee_no") val employeeNo: Int,
    @KomapperColumn("employee_name") val employeeName: String,
)

data class RobotInfo2(
    val hiredate: LocalDate? = null,
    val salary: BigDecimal? = null,
)

@KomapperEntity
@KomapperTable("employee")
data class Robot(
    @KomapperId
    @KomapperColumn("employee_id")
    val employeeId: Int,
    @KomapperEmbedded
    @KomapperColumnOverride("employeeNo", KomapperColumn("employee_no"))
    @KomapperColumnOverride("employeeName", KomapperColumn("employee_name"))
    val info1: RobotInfo1,
    @KomapperEmbedded
    val info2: RobotInfo2?,
    @KomapperColumn("manager_id") val managerId: Int?,
    @KomapperColumn("department_id") val departmentId: Int,
    @KomapperColumn("address_id") val addressId: Int,
    @KomapperVersion val version: Int,
)

@KomapperEntity
@KomapperTable("employee")
data class Android(
    @KomapperId
    @KomapperColumn("employee_id")
    val employeeId: Int,
    @KomapperEmbedded
    @KomapperColumnOverride("first", KomapperColumn("employee_no"))
    @KomapperColumnOverride("second", KomapperColumn("employee_name"))
    val info1: Pair<Int, String>,
    @KomapperEmbedded
    @KomapperColumnOverride("first", KomapperColumn("hiredate"))
    @KomapperColumnOverride("second", KomapperColumn("salary"))
    val info2: Pair<LocalDate?, BigDecimal?>?,
    @KomapperColumn("manager_id") val managerId: Int?,
    @KomapperColumn("department_id") val departmentId: Int,
    @KomapperColumn("address_id") val addressId: Int,
    @KomapperVersion val version: Int,
)

typealias CyborgInfo1 = Pair<Int, String>
typealias CyborgInfo2 = Pair<LocalDate?, BigDecimal?>

data class Cyborg(
    val employeeId: Int,
    val info1: CyborgInfo1,
    val info2: CyborgInfo2?,
    val managerId: Int?,
    val departmentId: Int,
    val addressId: Int,
    val version: Int,
)

@KomapperEntityDef(Cyborg::class)
@KomapperTable("employee")
data class CyborgDef(
    @KomapperId
    @KomapperColumn("employee_id")
    val employeeId: Nothing,
    @KomapperEmbedded
    @KomapperColumnOverride("first", KomapperColumn("employee_no"))
    @KomapperColumnOverride("second", KomapperColumn("employee_name"))
    val info1: Nothing,
    @KomapperEmbedded
    @KomapperColumnOverride("first", KomapperColumn("hiredate"))
    @KomapperColumnOverride("second", KomapperColumn("salary"))
    val info2: Nothing,
    @KomapperColumn("manager_id") val managerId: Nothing,
    @KomapperColumn("department_id") val departmentId: Nothing,
    @KomapperColumn("address_id") val addressId: Nothing,
    @KomapperVersion val version: Nothing,
)

data class MachineInfo1(
    val employeeNo: Int,
    val employeeName: String,
)

data class MachineInfo2(
    val hiredate: LocalDate? = null,
    val salary: BigDecimal? = null,
)

@KomapperEntity
@KomapperTable("employee")
data class Machine(
    @KomapperId
    @KomapperColumn("employee_id")
    val employeeId: Int,
    @KomapperEmbedded
    @KomapperColumnOverride("employeeNo", KomapperColumn("employee_no"))
    @KomapperColumnOverride("employeeName", KomapperColumn("employee_name", alternateType = ClobString::class))
    val info1: MachineInfo1,
    @KomapperEmbedded
    val info2: MachineInfo2?,
    @KomapperColumn("manager_id") val managerId: Int?,
    @KomapperColumn("department_id") val departmentId: Int,
    @KomapperColumn("address_id") val addressId: Int,
    @KomapperVersion val version: Int,
)

@KomapperAggregateRoot("departments")
@KomapperOneToMany(Employee::class, navigator = "employees")
@KomapperEntity
data class Department(
    @KomapperId
    @KomapperColumn("department_id")
    val departmentId: Int,
    @KomapperColumn("department_no") val departmentNo: Int,
    @KomapperColumn("department_name") val departmentName: String,
    val location: String,
    @KomapperVersion val version: Int,
)

@KomapperEntity
@KomapperTable("department")
data class NoVersionDepartment(
    @KomapperId
    @KomapperColumn("department_id")
    val departmentId: Int,
    @KomapperColumn("department_no") val departmentNo: Int,
    @KomapperColumn("department_name") val departmentName: String,
    val location: String,
    val version: Int,
)

data class Place(
    val id: Int,
    val street: String,
    val version: Int,
)

typealias LocationInt = Int
typealias LocationString = String

@KomapperEntity
@KomapperTable("address")
data class Location(
    @KomapperId
    @KomapperColumn(name = "address_id")
    val id: LocationInt?,
    val street: LocationString,
    @KomapperVersion
    val version: LocationInt,
)

@KomapperEntity
data class T(
    @KomapperId(virtual = true)
    val n: Int,
)

@KomapperEntity
data class IntPair(
    @KomapperId(virtual = true)
    val first: Int,
    @KomapperId(virtual = true)
    val second: Int,
)

@KomapperEntity
data class NameAndAmount(
    @KomapperId(virtual = true)
    val name: String,
    val amount: BigDecimal,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NameAndAmount

        if (name != other.name) return false
        if (amount.compareTo(other.amount) != 0) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }
}

@KomapperEntity
data class EmployeeSalary(
    @KomapperId(virtual = true)
    val departmentId: Int,
    @KomapperId(virtual = true)
    val employeeName: String,
    @KomapperId(virtual = true)
    val salary: BigDecimal,
    val averageSalary: BigDecimal,
)
