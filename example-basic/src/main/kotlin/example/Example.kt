package example

import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmCreatedAt
import org.komapper.annotation.KmEntityDef
import org.komapper.annotation.KmId
import org.komapper.annotation.KmUpdatedAt
import org.komapper.annotation.KmVersion
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.SchemaQuery
import org.komapper.core.dsl.runQuery
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
    @KmId @KmAutoIncrement @KmColumn(name = "ADDRESS_ID")
    val id: String,
    @KmVersion val version: String,
    @KmCreatedAt val createdAt: String,
    @KmUpdatedAt val updatedAt: String,
)

fun main() {
    // create a Database instance
    val db = Database.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = Address.alias

    // execute simple CRUD operations as a transaction
    db.transaction {
        // create a schema
        db.runQuery {
            SchemaQuery.create(a)
        }

        // CREATE
        val newAddress = db.runQuery {
            EntityQuery.insert(a, Address(street = "street A"))
        }

        // READ: select by id
        val address1 = db.runQuery {
            EntityQuery.first(a) { a.id eq newAddress.id }
        }
        println("address1=$address1")

        // UPDATE
        db.runQuery {
            EntityQuery.update(a, address1.copy(street = "street B"))
        }

        // READ: select by street
        val address2 = db.runQuery {
            EntityQuery.first(a) { a.street eq "street B" }
        }
        println("address2=$address2")

        // DELETE
        db.runQuery {
            EntityQuery.delete(a, address2)
        }

        // READ: select all
        val list = db.runQuery {
            EntityQuery.from(a).orderBy(a.id)
        }
        println("list:$list")
        check(list.isEmpty()) { "The list must be empty." }
    }
}
