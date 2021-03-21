Komapper: Kotlin SQL Mapper
===========================

[![Build](https://github.com/nakamura-to/komapper/actions/workflows/build.yml/badge.svg)](https://github.com/nakamura-to/komapper/actions/workflows/build.yml)

Komapper is a simple database access library for Kotlin.

Supported databases are as follows:

- H2 1.4.199 or above

## Strengths

- Generate meta-models at compile-time using [google/ksp](https://github.com/google/ksp).
- Flexible and natural syntax for criteria queries.
- SQL templates, called “two-way SQL”.

## Status

In Development

## Example

```kotlin
@KmEntity
data class Address(
    @KmId
    @KmIdentityGenerator
    @KmColumn(name = "ADDRESS_ID")
    val id: Int = 0,
    val street: String,
    @KmVersion val version: Int = 0
) {
    companion object
}

fun main() {
    // create a Database instance
    val db = Database(H2DatabaseConfig("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1"))

    // set up schema
    db.transaction {
        @Language("sql")
        val sql = """
            CREATE TABLE ADDRESS(
                ADDRESS_ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                STREET VARCHAR(20) UNIQUE,
                VERSION INTEGER
            );
        """.trimIndent()
        db.script(sql)
    }

    // create a metamodel
    val a = Address.metamodel()

    // execute simple CRUD operations as a transaction
    db.transaction {
        // CREATE
        val addressA = db.insert(a, Address(street = "street A"))
        println(addressA)

        // READ: select by id
        val foundA = db.find(a) {
            a.id eq addressA.id
        }
        check(addressA == foundA)

        // UPDATE
        val addressB = db.update(a, addressA.copy(street = "street B"))
        println(addressB)

        // READ: select by street and version
        val foundB1 = db.find(a) {
            a.street eq "street B"
            a.version eq 1
        }
        check(addressB == foundB1)

        // READ: select using template
        val foundB2 = db.first {
            data class Params(val street: String)

            @Language("sql")
            val sql = "select ADDRESS_ID, STREET, VERSION from Address where street = /*street*/'test'"
            TemplateQuery.select(sql, Params("street B")) {
                Address(asInt("ADDRESS_ID"), asString("STREET"), asInt("VERSION"))
            }
        }
        check(addressB == foundB2)

        // DELETE
        db.delete(a, addressB)

        // READ: select all
        val addressList = db.list {
            EntityQuery.from(a).orderBy(a.id)
        }
        check(addressList.isEmpty())
    }
}

fun check(value: Boolean) {
    if (!value) error("failed.")
}
```

## Twitter

Author: @nakamura_to  
hashtag: #komapper

## License

```
Copyright 2021 Toshihiro Nakamura

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
