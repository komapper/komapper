package example

import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmCreatedAt
import org.komapper.annotation.KmEntityDef
import org.komapper.annotation.KmId
import org.komapper.annotation.KmIdentityGenerator
import org.komapper.annotation.KmUpdatedAt
import org.komapper.annotation.KmVersion
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.SchemaQuery
import org.komapper.core.dsl.runQuery
import org.komapper.jdbc.h2.H2DatabaseConfig
import org.komapper.transaction.transaction
import java.time.LocalDateTime

data class Address(
    val id: Int = 0,
    val street: String,
    val version: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    companion object
}

@KmEntityDef(Address::class)
private data class AddressDef(
    @KmId @KmIdentityGenerator @KmColumn(name = "ADDRESS_ID")
    val id: String,
    @KmVersion val version: String,
    @KmCreatedAt val createdAt: String,
    @KmUpdatedAt val updatedAt: String,
)

fun main() {
    // create a Database instance
    val db = Database(H2DatabaseConfig("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1"))

    // get a metamodel
    val a = Address.alias

    // execute simple CRUD operations as a transaction
    db.transaction {
        // create a schema
        db.runQuery {
            SchemaQuery.create(a)
        }

        // CREATE
        val id = db.runQuery {
            EntityQuery.insert(a, Address(street = "street A"))
        }

        // READ: select by id
        val addressA = db.runQuery {
            EntityQuery.first(a) {
                a.id eq id
            }
        }
        println(addressA)

        // UPDATE
        val addressB = db.runQuery {
            EntityQuery.update(a, addressA.copy(street = "street B"))
        }
        println(addressB)

        // READ: select by street and version
        val addressC = db.runQuery {
            EntityQuery.first(a) {
                a.street eq "street B"
                a.version eq 1
            }
        }
        println(addressC)

        // DELETE
        db.runQuery {
            EntityQuery.delete(a, addressC)
        }

        // READ: select all
        val list = db.runQuery {
            EntityQuery.from(a).orderBy(a.id)
        }
        check(list.isEmpty())
    }
}

fun check(value: Boolean) {
    if (!value) error("failed.")
}
