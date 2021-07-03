Komapper: Kotlin SQL Mapper
===========================

[![Build](https://github.com/komapper/komapper/actions/workflows/build.yml/badge.svg)](https://github.com/komapper/komapper/actions/workflows/build.yml)
[![Twitter](https://img.shields.io/badge/twitter-@komapper-pink.svg?style=flat)](https://twitter.com/komapper)

Komapper is a simple database access library for Kotlin 1.5 and later.

For more documentation, go to our site: https://www.komapper.org/docs/.

## Features

- Support both JDBC and R2DBC
- Most features work without reflection, thanks to [KSP](https://github.com/google/ksp)
- Entity classes do not require any annotations
- Support Kotlin value classes
- Provide Spring Boot autoconfigures

## Status

This project is still in development, all suggestions and contributions are welcome.

## Examples

### Entity class and mapping definition

```kotlin
// Entity class: this class does not require any annotations.
data class Address(
    val id: Int = 0,
    val street: String,
    val version: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

// Mapping definition: this class maps the ADDRESS table and the Address class.
@KomapperEntityDef(Address::class)
data class AddressDef(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "ADDRESS_ID")
    val id: Nothing,
    @KomapperVersion val version: Nothing,
    @KomapperCreatedAt val createdAt: Nothing,
    @KomapperUpdatedAt val updatedAt: Nothing,
) {
    companion object
}
```

### Entity DSL

#### Select

```kotlin
// get a generated metamodel
val a = AddressDef.meta

// select all
EntityDsl.from(a)

// select by id
EntityDsl.from(a).first { a.id eq 1 }

// select by multiple conditions
EntityDsl.from(a).where {
    a.street like "A%"
    a.createdAt greater LocalDateTime.of(2020, 1, 1, 0, 0)
}.orderBy(a.id)


val e = EmployeeDef.meta
val d = DepartmentDef.meta

// select using join operations
EntityDsl.from(e)
    .innerJoin(a) { e.addressId eq a.id }
    .leftJoin(d) { e.departmentId eq d.id }
    .where { d.location eq "TOKYO" }
    .orderBy(e.id)
    .associate(e, a) { employee, address -> employee.copy(address = address) }
    .associate(e, d) { employee, department -> employee.copy(department = department) }
```

#### Insert

```kotlin
val a = AddressDef.meta

// insert single entity
EntityDsl.insert(a).single(Address(street = "STREET A"))

// insert multiple entities at once
EntityDsl.insert(a).multiple(
    Address(street = "STREET A"),
    Address(street = "STREET B"),
)

// insert or update single entity
EntityDsl.insert(a)
    .onDuplicateKeyUpdate(a.street)
    .single(Address(street = "STREET A"))
```

#### Update

```kotlin
val a = AddressDef.meta

val address = ...

// update single entity
EntityDsl.update(a).single(address)
```

#### Delete

```kotlin
val a = AddressDef.meta

val address = ...

// delete single entity
EntityDsl.delete(a).single(address)
```

### SQL DSL

#### Select

```kotlin
val e = EmployeeDef.meta
val d = DepartmentDef.meta

// union
val q1 = SqlDsl.from(e).select(e.name)
val q2 = SqlDsl.from(d).select(d.name)
val unionQuery = q1 union q2

// group by and having
SqlDsl.from(e)
    .groupBy(e.departmentId)
    .having { count(e.id) greaterEq 4 }
    .orderBy(e.departmentId)
    .select(e.departmentId, count(e.id))
```

#### Insert

```kotlin
val a = AddressDef.meta

SqlDsl.insert(a).values {
    a.street set "STREET 16"
    a.version set 0
    a.createdAt set LocalDateTime.now()
    a.updatedAt set LocalDateTime.now()
}
```

#### Update

```kotlin
val a = AddressDef.meta

SqlDsl.update(a).set {
    a.street set "STREET X"
    a.version set (a.version + 10)
}.where {
    a.id eq 1
}
```

#### Delete

```kotlin
val a = AddressDef.meta

SqlDsl.delete(a).where {
    a.id inList listOf(1, 2, 3)
}
```