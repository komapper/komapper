package example

import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmCreatedAt
import org.komapper.annotation.KmEntityDef
import org.komapper.annotation.KmId
import org.komapper.annotation.KmUpdatedAt
import org.komapper.annotation.KmVersion
import org.komapper.core.Database
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.SchemaDsl
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

@Suppress("unused")
@KmEntityDef(Address::class)
private data class AddressDef(
    @KmId @KmAutoIncrement @KmColumn(name = "ADDRESS_ID")
    val id: Nothing,
    @KmVersion val version: Nothing,
    @KmCreatedAt val createdAt: Nothing,
    @KmUpdatedAt val updatedAt: Nothing,
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
            SchemaDsl.create(a)
        }

        // CREATE
        val newAddress = db.runQuery {
            EntityDsl.insert(a).single(Address(street = "street A"))
        }

        // READ: select by id
        val address1 = db.runQuery {
            EntityDsl.from(a).first { a.id eq newAddress.id }
        }
        println("address1=$address1")

        // UPDATE
        db.runQuery {
            EntityDsl.update(a).single(address1.copy(street = "street B"))
        }

        // READ: select by street
        val address2 = db.runQuery {
            EntityDsl.from(a).first { a.street eq "street B" }
        }
        println("address2=$address2")

        // DELETE
        db.runQuery {
            EntityDsl.delete(a).single(address2)
        }

        // READ: select all
        val list = db.runQuery {
            EntityDsl.from(a).orderBy(a.id)
        }
        println("list:$list")
        check(list.isEmpty()) { "The list must be empty." }
    }
}
