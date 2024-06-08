Komapper: Kotlin ORM for JDBC and R2DBC
========================================

[![Build](https://github.com/komapper/komapper/actions/workflows/build.yml/badge.svg)](https://github.com/komapper/komapper/actions/workflows/build.yml)
[![Twitter](https://img.shields.io/badge/News-@komapper-0071BC.svg?style=flat&logo=twitter)](https://twitter.com/komapper)
[![Slack](https://img.shields.io/badge/Chat-%23komapper-yellow.svg?style=flat&logo=slack)](https://kotlinlang.slack.com/messages/komapper/)
[![Maven Central](https://img.shields.io/maven-central/v/org.komapper/komapper-platform)](https://search.maven.org/artifact/org.komapper/komapper-platform)

Komapper is an ORM library for server-side Kotlin.

For more documentation, go to our site:  
- https://www.komapper.org/docs/

## Features

### Highlighted

- Support for both JDBC and R2DBC
- Code generation at compile-time using [Kotlin Symbol Processing API](https://github.com/google/ksp)
- Immutable and composable queries
- Support for Kotlin value classes
- Easy Spring Boot integration

### Experimental

- Quarkus integration
- Transaction management using context receivers

## Prerequisite

- Kotlin 1.8.22 or later
- JRE 11 or later
- Gradle 6.8.3 or later

## Supported Databases

Komapper is tested with the following databases:

| Database           | version | JDBC support | R2DBC support |
|--------------------|---------|:------------:|:-------------:|
| H2 Database        | 2.2.222 |      v       |       v       |
| MariaDB            | 10.6.3  |      v       |       v       |
| MySQL v5.x         | 5.7.44  |      v       |       v       |
| MySQL v8.x         | 8.0.25  |      v       |       v       |
| Oracle Database XE | 18.4.0  |      v       |       v       |
| PostgreSQL         | 12.9    |      v       |       v       |
| SQL Server         | 2019    |      v       |  (unstable)   |

Supported connectivity types:

- JDBC 4.3
- R2DBC 1.0.0

## Installation

Add the following code to the Gradle build script (gradle.build.kts).

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.22"
}

val komapperVersion = "1.18.1"

dependencies {
    platform("org.komapper:komapper-platform:$komapperVersion").let {
        implementation(it)
        ksp(it)
    }
    implementation("org.komapper:komapper-starter-jdbc")
    implementation("org.komapper:komapper-dialect-h2-jdbc")
    ksp("org.komapper:komapper-processor")
}
```

See also Quickstart for more details:

- https://www.komapper.org/docs/quickstart/

## Sample code

To get complete code, go to our [example repository](https://github.com/komapper/komapper-examples).

### Entity class definition

```kotlin
@KomapperEntity
data class Address(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "ADDRESS_ID")
    val id: Int = 0,
    val street: String,
    @KomapperVersion
    val version: Int = 0,
    @KomapperCreatedAt
    val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt
    val updatedAt: LocalDateTime? = null,
)
```

### Connecting with JDBC

```kotlin
fun main() {
    // create a Database instance
    val db = JdbcDatabase("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = Meta.address

    // execute simple operations in a transaction
    db.withTransaction {
        // create a schema
        db.runQuery {
            QueryDsl.create(a)
        }

        // INSERT
        val newAddress = db.runQuery {
            QueryDsl.insert(a).single(Address(street = "street A"))
        }

        // SELECT
        val address = db.runQuery {
            QueryDsl.from(a).where { a.id eq newAddress.id }.first()
        }
    }
}
```

### Connecting with R2DBC
```kotlin
suspend fun main() {
    // create a Database instance
    val db = R2dbcDatabase("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = Meta.address

    // execute simple operations in a transaction
    db.withTransaction {
        // create a schema
        db.runQuery {
            QueryDsl.create(a)
        }

        // INSERT
        val newAddress = db.runQuery {
            QueryDsl.insert(a).single(Address(street = "street A"))
        }

        // SELECT
        val address = db.runQuery {
            QueryDsl.from(a).where { a.id eq newAddress.id }.first()
        }
    }
}
```

## Design Policy

See [DESIGN_DOC](DESIGN_DOC.md) for the design policy of this project.

## Compatibility Matrix

| Kotlin and KSP version | Komapper version | KSP2 support? | JRE min version | Gradle min version |
|:-----------------------|:-----------------|---------------|:----------------|--------------------|
| 1.8.22-1.0.11          | 1.3.x - 1.16.x   | No            | 11              | 6.8.3              |
| 1.9.24-1.0.20          | 1.3.x - 1.18.x   | No            | 11              | 6.8.3              |
| 2.0.0-1.0.22           | 1.3.x - 1.18.0   | No            | 11              | 6.8.3              |
| 2.0.0-1.0.22           | 1.18.1           | Yes           | 11              | 6.8.3              |

Compatibility testing is performed in the [komapper/compatibility-test](https://github.com/komapper/compatibility-test/) repository.

## Backers & Sponsors

If you use Komapper in your project or enterprise and would like to support ongoing development, please consider becoming a backer or a sponsor.
Sponsor logos will show up here with a link to your website.

[[Become a backer or a sponsor](https://opencollective.com/komapper#category-CONTRIBUTE)]
