Komapper: Kotlin SQL Mapper
===========================

[![Build](https://github.com/komapper/komapper/actions/workflows/build.yml/badge.svg)](https://github.com/komapper/komapper/actions/workflows/build.yml)
[![Twitter](https://img.shields.io/badge/twitter-@komapper-pink.svg?style=flat)](https://twitter.com/komapper)

Komapper is a simple database access library for Kotlin 1.5 and later.

For more documentation, go to our site: https://www.komapper.org/docs/.

## Features

- compile-time code generation using [Kotlin Symbol Processing API](https://github.com/google/ksp)
- annotation-free data models
- value class support
- immutable and composable queries
- upsert (insert-or-update) query support

## Examples
### Data model and definition

```kotlin
// Data model: this class doesn't require any annotations.
data class Address(
    val id: Int = 0,
    val street: String,
    val version: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

// Definition: this class maps the ADDRESS table and the Address class.
@KmEntityDef(Address::class)
data class AddressDef(
    @KmId @KmAutoIncrement @KmColumn(name = "ADDRESS_ID")
    val id: Nothing,
    @KmVersion val version: Nothing,
    @KmCreatedAt val createdAt: Nothing,
    @KmUpdatedAt val updatedAt: Nothing,
) {
    companion object
}
```

### Queries
#### Select
```kotlin
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

// composable query
val w1: WhereDeclaration = {
    a.street like "A%"
}
val w2: WhereDeclaration = {
    a.createdAt greater LocalDateTime.of(2020, 1, 1, 0, 0)
}
EntityDsl.from(a).where(w1 + w2).orderBy(a.id)
```

#### Insert
```kotlin
val a = AddressDef.meta

// insert single entity
EntityDsl.insert(a).single(Address(street = "STREET A"))

// insert multiple entity at once
EntityDsl.insert(a).multiple(
    Address(street = "STREET A"),
    Address(street = "STREET B"),
)

// insert or update single entity
EntityDsl.insert(a).onDuplicateKeyUpdate(a.street).single(
    Address(street = "STREET A")
)
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
