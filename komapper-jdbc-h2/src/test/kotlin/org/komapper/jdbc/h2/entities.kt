package org.komapper.jdbc.h2

import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmCreatedAt
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmIdentityGenerator
import org.komapper.annotation.KmIgnore
import org.komapper.annotation.KmSequenceGenerator
import org.komapper.annotation.KmTable
import org.komapper.annotation.KmUpdatedAt
import org.komapper.annotation.KmVersion
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@KmEntity
data class Address(
    @KmId @KmColumn(name = "ADDRESS_ID") val addressId: Int,
    val street: String,
    @KmVersion val version: Int
) {
    companion object
}

@KmEntity
@KmTable("IDENTITY_STRATEGY")
data class IdentityStrategy(
    @KmId @KmIdentityGenerator val id: Int?,
    val value: String
) {
    companion object
}

@KmEntity
@KmTable("SEQUENCE_STRATEGY")
data class SequenceStrategy(
    @KmId @KmSequenceGenerator(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100) val id: Int,
    val value: String
) {
    companion object
}

@KmEntity
data class Person(
    @KmId @KmColumn("PERSON_ID") val personId: Int,
    val name: String,
    @KmCreatedAt @KmColumn("CREATED_AT") val createdAt: LocalDateTime? = null,
    @KmUpdatedAt @KmColumn("UPDATED_AT") val updatedAt: LocalDateTime? = null
) {
    companion object
}

@KmEntity
@KmTable("PERSON")
data class Human(
    @KmId @KmColumn("PERSON_ID") val humanId: Int,
    val name: String,
    @KmCreatedAt val createdAt: OffsetDateTime? = null,
    @KmUpdatedAt val updatedAt: OffsetDateTime? = null
) {
    companion object
}

@KmEntity
data class Employee(
    @KmId @KmColumn("EMPLOYEE_ID") val employeeId: Int,
    @KmColumn("EMPLOYEE_NO") val employeeNo: Int,
    @KmColumn("EMPLOYEE_NAME") val employeeName: String,
    @KmColumn("MANAGER_ID") val managerId: Int?,
    val hiredate: LocalDate,
    val salary: BigDecimal,
    @KmColumn("DEPARTMENT_ID") val departmentId: Int,
    @KmColumn("ADDRESS_ID") val addressId: Int,
    @KmVersion val version: Int,
    @KmIgnore val address: Address? = null,
    @KmIgnore val department: Department? = null
) {
    companion object
}

@KmEntity
data class Department(
    @KmId @KmColumn("DEPARTMENT_ID") val departmentId: Int,
    @KmColumn("DEPARTMENT_NO") val departmentNo: Int,
    @KmColumn("DEPARTMENT_NAME") val departmentName: String,
    val location: String,
    @KmVersion val version: Int,
    @KmIgnore val employeeList: List<Employee> = emptyList()
) {
    companion object
}
