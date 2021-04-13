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
import org.komapper.core.dsl.execute
import org.komapper.jdbc.h2.H2DatabaseConfig
import java.time.LocalDateTime

data class Address(
    val id: Int = 0,
    val street: String,
    val version: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

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
    val db = Database(H2DatabaseConfig("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1", enableTransaction = true))

    // create a metamodel
    val a = Address_()

    // execute simple CRUD operations as a transaction
    db.transaction {
        // create a schema
        db.execute {
            SchemaQuery.create(a)
        }

        // CREATE
        val addressA = db.execute {
            EntityQuery.insert(a, Address(street = "street A"))
        }
        println(addressA)

        // READ: select by id
        val foundA = db.execute {
            EntityQuery.first(a) {
                a.id eq addressA.id
            }
        }
        check(addressA == foundA)

        // UPDATE
        val addressB = db.execute {
            EntityQuery.update(a, addressA.copy(street = "street B"))
        }
        println(addressB)

        // READ: select by street and version
        val foundB1 = db.execute {
            EntityQuery.first(a) {
                a.street eq "street B"
                a.version eq 1
            }
        }
        check(addressB == foundB1)

        // DELETE
        db.execute {
            EntityQuery.delete(a, addressB)
        }

        // READ: select all
        val addressList = db.execute {
            EntityQuery.from(a).orderBy(a.id)
        }
        check(addressList.isEmpty())
    }
}

fun check(value: Boolean) {
    if (!value) error("failed.")
}
