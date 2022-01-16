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

- Support for both JDBC and R2DBC
- Code generation at compile-time using [Kotlin Symbol Processing API](https://github.com/google/ksp)
- Immutable and composable queries
- Support for Kotlin value classes
- Easy Spring Boot integration

## Prerequisite

- Kotlin 1.3.51 or later
- Java 11 or later
- Gradle 7.2 or later

## Supported connectivity types

- JDBC 4.3
- R2DBC [Arabba-SR12](https://r2dbc.io/2022/01/13/r2dbc-arabba-sr12-released)

## Supported Databases

- H2 Database Engine
- MariaDB
- MySQL
- PostgreSQL

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

### Connect using JDBC

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
            SchemaDsl.create(a)
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

### Connect using R2DBC
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
            SchemaDsl.create(a)
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

## Compare with Kotlin Exposed

The following code is equivalent to the [SQL DSL example](https://github.com/JetBrains/Exposed#sql-dsl) in Kotlin Exposed:

```kotlin
package org.komapper.example

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.SchemaDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.substring
import org.komapper.core.dsl.operator.trim
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import org.komapper.tx.jdbc.withTransaction

data class User(
    val id: String,
    val name: String,
    val cityId: Int?
)

data class City(
    val id: Int? = null,
    val name: String
)

@KomapperEntityDef(User::class)
@KomapperTable("Users")
data class UserDef(
    @KomapperId
    val id: Nothing
)

@KomapperEntityDef(City::class)
@KomapperTable("Cities")
data class CityDef(
    @KomapperId
    @KomapperAutoIncrement
    val id: Nothing
)

fun main() {
    val c = Meta.city
    val u = Meta.user

    val db = JdbcDatabase.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    db.withTransaction {
        db.runQuery {
            SchemaDsl.create(c, u)
        }

        val (saintPetersburg, munich) = db.runQuery {
            QueryDsl.insert(c).multiple(
                City(name = "St. Petersburg"),
                City(name = "Munich"),
            )
        }

        val (_, pragueId) = db.runQuery {
            QueryDsl.insert(c).values { c.name eq substring(trim(literal("   Prague   ")), 1, 2) }
        }

        val prague = db.runQuery {
            QueryDsl.from(c).where { c.id eq pragueId }.first()
        }
        check(prague.name == "Pr") { prague.toString() }

        db.runQuery {
            QueryDsl.insert(u).multiple(
                User(id = "andrey", name = "Andrey", cityId = saintPetersburg.id),
                User(id = "sergey", name = "Sergey", cityId = munich.id),
                User(id = "eugene", name = "Eugene", cityId = munich.id),
                User(id = "alex", name = "Alex", cityId = null),
                User(id = "smth", name = "Something", cityId = null),
            )
        }

        db.runQuery {
            QueryDsl.update(u).set { u.name eq "Alexey" }.where { u.id eq "alex" }
        }

        db.runQuery {
            QueryDsl.delete(u).where { u.name like "%thing" }
        }

        println("All cities:")

        for (city in db.runQuery { QueryDsl.from(c) }) {
            println("${city.id}: ${city.name}")
        }

        println("Manual join:")

        db.runQuery {
            QueryDsl.from(u)
                .innerJoin(c) {
                    u.cityId eq c.id
                }.where {
                    and {
                        u.id eq "andrey"
                        or { u.name eq "Sergey" }
                    }
                    u.id eq "sergey"
                    u.cityId eq c.id
                }.select(u.name, c.name)
        }.forEach { (userName, cityName) ->
            println("$userName lives in $cityName")
        }

        println("Join with foreign key:")

        db.runQuery {
            QueryDsl.from(u)
                .innerJoin(c) {
                    u.cityId eq c.id
                }.where {
                    c.name eq "St. Petersburg"
                    or { u.cityId.isNull() }
                }.select(u.name, u.cityId, c.name)
        }.forEach { (userName, cityId, cityName) ->
            if (cityId != null) {
                println("$userName lives in $cityName")
            } else {
                println("$userName lives nowhere")
            }
        }

        println("Functions and group by:")

        db.runQuery {
            QueryDsl.from(c)
                .innerJoin(u) {
                    c.id eq u.cityId
                }.groupBy(c.name)
                .select(c.name, count(u.id))
        }.forEach { (cityName, userCount) ->
            if (userCount != null && userCount > 0L) {
                println("$userCount user(s) live(s) in $cityName")
            } else {
                println("Nobody lives in $cityName")
            }
        }

        db.runQuery {
            SchemaDsl.drop(u, c)
        }
    }
}
```

Generated SQL:

```sql
org.komapper.SQL_WITH_ARGS - create table if not exists Cities (ID integer auto_increment, NAME varchar(500) not null, constraint pk_Cities primary key(ID));create table if not exists Users (ID varchar(500) not null, NAME varchar(500) not null, CITY_ID integer, constraint pk_Users primary key(ID));
org.komapper.SQL_WITH_ARGS - insert into Cities (NAME) values ('St. Petersburg'), ('Munich')
org.komapper.SQL_WITH_ARGS - insert into Cities (NAME) values ((substring((trim('   Prague   ')), 1, 2)))
org.komapper.SQL_WITH_ARGS - select t0_.ID, t0_.NAME from Cities as t0_ where t0_.ID = 3
org.komapper.SQL_WITH_ARGS - insert into Users (ID, NAME, CITY_ID) values ('andrey', 'Andrey', 1), ('sergey', 'Sergey', 2), ('eugene', 'Eugene', 2), ('alex', 'Alex', null), ('smth', 'Something', null)
org.komapper.SQL_WITH_ARGS - update Users as t0_ set NAME = 'Alexey' where t0_.ID = 'alex'
org.komapper.SQL_WITH_ARGS - delete from Users as t0_ where t0_.NAME like '%thing' escape '\'
All cities:
org.komapper.SQL_WITH_ARGS - select t0_.ID, t0_.NAME from Cities as t0_
1: St. Petersburg
2: Munich
3: Pr
Manual join:
org.komapper.SQL_WITH_ARGS - select t0_.NAME, t1_.NAME from Users as t0_ inner join Cities as t1_ on (t0_.CITY_ID = t1_.ID) where (t0_.ID = 'andrey' or (t0_.NAME = 'Sergey')) and t0_.ID = 'sergey' and t0_.CITY_ID = t1_.ID
Sergey lives in Munich
Join with foreign key:
org.komapper.SQL_WITH_ARGS - select t0_.NAME, t0_.CITY_ID, t1_.NAME from Users as t0_ inner join Cities as t1_ on (t0_.CITY_ID = t1_.ID) where t1_.NAME = 'St. Petersburg' or (t0_.CITY_ID is null)
Andrey lives in St. Petersburg
Functions and group by:
org.komapper.SQL_WITH_ARGS - select t0_.NAME, count(t1_.ID) from Cities as t0_ inner join Users as t1_ on (t0_.ID = t1_.CITY_ID) group by t0_.NAME
2 user(s) live(s) in Munich
1 user(s) live(s) in St. Petersburg
org.komapper.SQL_WITH_ARGS - drop table if exists Users;drop table if exists Cities;
```

See the [comparison-with-exposed](https://github.com/komapper/komapper-examples/tree/main/comparison-with-exposed) project to get complete code.