# Design Doc

## Overview

This document presents design policy for Komapper.

## The goal of Komapper

Komapper provides a high-level database access API for server-side Kotlin.

## Background

Comparing Java and Kotlin, Kotlin has the following attractions that may motivate us to use Kotlin:

- Null Safety, Data Classes, Properties, and many other useful language features
- Full range of collection APIs
- Coroutines allow concise handling of non-blocking processes

We believe that we can create a better database access library
by taking full advantage of Kotlin's features as described above.

## Differences from existing libraries

As of March 2022, there are two well-known database access libraries written in Kotlin.

- [Exposed](https://github.com/JetBrains/Exposed)
- [Ktorm](https://github.com/kotlin-orm/ktorm)

Unlike these libraries, Komapper has the following policies:

- Support both JDBC and R2DBC
- Provide SQL templates as well as type-safe queries
- Avoid reflection calls and database metadata reading at runtime

## Architectural considerations

The main items for consideration are as follows:

- Compile-time code generation
- Immutable data model
- Separation of query construction and execution
- Loosely coupled architecture
- Less data cache

### Compile-time code generation

To avoid reflection calls and database metadata reading, 
we chose to generate code at compile-time using the [Kotlin Symbol Processing API](https://github.com/google/ksp).

The reasons to avoid reflection calls are as follows:

- Code becomes more complex
- Errors are more likely to occur at runtime
- Additional configuration is required when converting to native image

The reasons to avoid database metadata reading as follows:

- It takes a long time to read database metadata.
Impact is often negligible in test environments where readings are repeated many times in a short period of time.

### Immutable data model

We decide that the Kotlin class mapped to the database table must be a Kotlin Data Class.
This is because, in general, defects are less likely to occur when an immutable data model is used.

### Separation of query construction and execution

We clearly separate the query construction and execution APIs
so that both JDBC and R2DBC specific APIs can use the same query object.

Here is an example code:

```kotlin
// get a metamodel
val a = Meta.address
// build a query object
val query = QueryDsl.from(a).where { a.street startsWith "TOKYO" }.orderBy(a.id)

// create JDBC database object
val jdbcDatabase = JdbcDatabase("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
// execute the above query object using JDBC
val jdbcResult = jdbcDatabase.runQuery(query)

// create R2DBC database object
val r2dbcDatabase = R2dbcDatabase("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")
// execute the above query object using R2DBC
val r2dbcResult = r2dbcDatabase.runQuery(query)
```

### Loosely coupled architecture

We split Komapper's functions into modules so that users can freely select and use only the functions they need.
To integrate the divided functions at runtime, we use
[ServiceLoader](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html).

### Less data cache

To avoid complexity, we generally do not cache data retrieved from the database.
However, as an exception, sequence values for generating IDs are kept inside Komapper.

## Test Automation

We test on all databases supported by Komapper.
Tests are run in GitHub Actions each time a pull request is received or merged into the main branch.

## Release Automation

We perform all the following tasks using GitHub Actions workflows:

- Version upgrade
- Tagging
- Publishing build artifacts to Maven repository
- Creating release notes
- Release announcement

## Known issues or concerns

It seems that some R2DBC drivers are not yet stable.