Komapper: Kotlin ORM for JDBC and R2DBC
========================================

[![Build](https://github.com/komapper/komapper/actions/workflows/build.yml/badge.svg)](https://github.com/komapper/komapper/actions/workflows/build.yml)
[![Twitter](https://img.shields.io/badge/News-@komapper-0071BC.svg?style=flat&logo=twitter)](https://twitter.com/komapper)
[![Slack](https://img.shields.io/badge/Chat-%23komapper-yellow.svg?style=flat&logo=slack)](https://kotlinlang.slack.com/messages/komapper/)

Komapper is an ORM library for server-side Kotlin.

For more documentation, go to our site:  
- https://www.komapper.org/docs/ (English version)
- https://www.komapper.org/ja/docs/ (Japanese version)

## Features

### Highlighted

- Support for both JDBC and R2DBC
- Code generation at compile-time using [Kotlin Symbol Processing API](https://github.com/google/ksp)
- Immutable and composable queries
- Support for Kotlin value classes
- Easy Spring Boot integration

### Experimental

- Quarkus integration
- Spring Native integration
- Transaction management using context receivers

## Prerequisite

- Kotlin 1.5.31 or later
- JRE 11 or later
- Gradle 7.2 or later

## Supported Databases

Komapper is tested with the following databases:

| Database           | version | JDBC support | R2DBC support |
|--------------------|---------|:------------:|:-------------:|
| H2 Database        | 2.1.212 |      v       |       v       |
| MariaDB            | 10.6.3  |      v       | (not working) |
| MySQL              | 8.0.25  |      v       | (not working) |
| Oracle Database XE | 18.4.0  |      v       |       v       |
| PostgreSQL         | 12.9    |      v       |       v       |
| SQL Server         | 2019    |      v       |  (unstable)   |

Supported connectivity types:

- JDBC 4.3
- R2DBC 0.9.1 and 1.0.0

## Installation

Add the following code to the Gradle build script (gradle.build.kts).

```kotlin
plugins {
    kotlin("jvm") version "1.6.21"
    id("com.google.devtools.ksp") version "1.6.21-1.0.5"
}

val komapperVersion = "1.1.1"

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

- https://www.komapper.org/docs/quickstart/ (English version)
- https://www.komapper.org/ja/docs/quickstart/ (Japanese version)

## Sample code

To get complete code, go to our [example repository](https://github.com/komapper/komapper-examples).

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
