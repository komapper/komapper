package example

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmCreatedAt
import org.komapper.annotation.KmEntityDef
import org.komapper.annotation.KmId
import org.komapper.annotation.KmUpdatedAt
import org.komapper.annotation.KmVersion
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl
import org.komapper.r2dbc.dsl.R2dbcSchemaDsl
import org.komapper.tx.r2dbc.transaction
import java.time.LocalDateTime

data class Address(
    val id: Int = 0,
    val street: String,
    val version: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

@KmEntityDef(Address::class)
data class AddressDef(
    @KmId @KmAutoIncrement @KmColumn(name = "ADDRESS_ID")
    val id: Nothing,
    @KmVersion val version: Nothing,
    @KmCreatedAt val createdAt: Nothing,
    @KmUpdatedAt val updatedAt: Nothing,
) {
    companion object
}

fun main() = runBlocking {
    // create a Database instance
    val db = R2dbcDatabase.create("r2dbc:h2:mem:///example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = AddressDef.meta

    // execute simple CRUD operations as a transaction
    db.transaction {
        // create a schema
        db.runQuery {
            R2dbcSchemaDsl.create(a)
        }

        // CREATE
        val newAddress = db.runQuery {
            R2dbcEntityDsl.insert(a).single(Address(street = "street A"))
        }

        // READ: select by id
        val address1 = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.id eq newAddress.id }
        }.first()

        println("address1 = $address1")

        // UPDATE
        db.runQuery {
            R2dbcEntityDsl.update(a).single(address1.copy(street = "street B"))
        }

        // READ: select by street
        val address2 = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.street eq "street B" }
        }.first()

        println("address2 = $address2")
        check(address1.id == address2.id)
        check(address1.street != address2.street)
        check(address1.version + 1 == address2.version)

        // DELETE
        db.runQuery {
            R2dbcEntityDsl.delete(a).single(address2)
        }

        // READ: select all
        val addressList = db.runQuery {
            R2dbcEntityDsl.from(a).orderBy(a.id)
        }.toList(mutableListOf())

        println("addressList = $addressList")
        check(addressList.isEmpty()) { "The addressList must be empty." }
    }
}
