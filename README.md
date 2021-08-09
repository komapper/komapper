Komapper: Kotlin SQL Mapper
===========================

[![Build](https://github.com/komapper/komapper/actions/workflows/build.yml/badge.svg)](https://github.com/komapper/komapper/actions/workflows/build.yml)
[![Twitter](https://img.shields.io/badge/twitter-@komapper-pink.svg?style=flat)](https://twitter.com/komapper)

Komapper is a database access library for server-side Kotlin.

For more documentation, go to our site:  
https://www.komapper.org/docs/.

日本語のドキュメントもあります（現時点では英語版より詳細です）   
https://www.komapper.org/ja/docs/

## Features

- Support for both JDBC and R2DBC
- Code generation at compile-time using [Kotlin Symbol Processing API](https://github.com/google/ksp)
- Immutable and composable queries
- Support for Kotlin value classes
- Easy Spring Boot integration

## Supported Databases

- H2 Database Engine
- MariaDB
- MySQL
- PostgreSQL

## Status

This project is still in development, all suggestions and contributions are welcome.

## Sample code

To get complete code, go to our example repository:  
https://github.com/komapper/komapper-examples

### Connect using JDBC

```kotlin
fun main() {
    // create a Database instance
    val db = JdbcDatabase.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = AddressDef.meta

    // execute simple operations in a transaction
    db.withTransaction {
        // create a schema
        db.runQuery {
            SchemaDsl.create(a)
        }

        // INSERT
        val newAddress = db.runQuery {
            EntityDsl.insert(a).single(Address(street = "street A"))
        }

        // SELECT
        val address = db.runQuery {
            EntityDsl.from(a).where { a.id eq newAddress.id }.first()
        }
    }
}
```

### Connect using R2DBC
```kotlin
fun main() = runBlocking {
    // create a Database instance
    val db = R2dbcDatabase.create("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = AddressDef.meta

    // execute simple operations in a transaction
    db.withTransaction {
        // create a schema
        db.runQuery {
            SchemaDsl.create(a)
        }

        // INSERT
        val newAddress = db.runQuery {
            EntityDsl.insert(a).single(Address(street = "street A"))
        }

        // SELECT
        val address = db.runQuery {
            EntityDsl.from(a).where { a.id eq newAddress.id }.first()
        }
    }
}
```
