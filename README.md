Komapper: Kotlin ORM for JDBC and R2DBC
========================================

[![Build](https://github.com/komapper/komapper/actions/workflows/build.yml/badge.svg)](https://github.com/komapper/komapper/actions/workflows/build.yml)
[![Twitter](https://img.shields.io/badge/twitter-@komapper-pink.svg?style=flat)](https://twitter.com/komapper)

Komapper is an ORM library for server-side Kotlin.

For more documentation, go to our site:  
https://www.komapper.org/docs/.

日本語のドキュメントもあります（現時点では英語版より詳細です）   
https://www.komapper.org/ja/docs/

## Features

### Highlighted features

- Support for both JDBC and R2DBC
- Code generation at compile-time using [Kotlin Symbol Processing API](https://github.com/google/ksp)
- Immutable and composable queries
- Support for Kotlin value classes
- Easy Spring Boot integration

### Experimentally supported features

- Quarkus integration
- Spring Native integration
 
## Prerequisite

- Kotlin 1.3.51 or later
- Java 11 or later
- Gradle 7.2 or later

## Supported Databases

Komapper is tested with the following databases:

| Database           | version | JDBC support | R2DBC support |
|--------------------|---------|:------------:|:-------------:|
| H2 Database        | 2.1.210 |      v       |       v       |
| MariaDB            | 10.6.3  |      v       |      N/A      |
| MySQL              | 8.0.25  |      v       |      N/A      |
| Oracle Database XE | 18.4.0  |      v       |       v       |
| PostgreSQL         | 12.9    |      v       |       v       |
| SQL Server         | 2019    |      v       |       v       |

Supported connectivity types are JDBC 4.3 and R2DBC 0.9.1.

## Installation

Add the following code to the Gradle build script (gradle.build.kts).

```kotlin
plugins {
    kotlin("jvm") version "1.5.31"
    id("com.google.devtools.ksp") version "1.5.31-1.0.1"
}

val komapperVersion = "0.27.0"

dependencies {
    implementation("org.komapper:komapper-starter-jdbc:$komapperVersion")
    implementation("org.komapper:komapper-dialect-h2-jdbc:$komapperVersion")
    ksp("org.komapper:komapper-processor:$komapperVersion")
}
```

See also Quickstart for more details:

- https://www.komapper.org/docs/quickstart/ (English)
- https://www.komapper.org/ja/docs/quickstart/ (日本語)

## Status

This project is still in development, all suggestions and contributions are welcome.

## Sample code

To get complete code, go to our example repository:  
https://github.com/komapper/komapper-examples

### Connecting with JDBC

```kotlin
fun main() {
    // create a Database instance
    val db = JdbcDatabase.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

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
fun main() = runBlocking {
    // create a Database instance
    val db = R2dbcDatabase.create("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")

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
