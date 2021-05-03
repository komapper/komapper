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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

val logger: Logger = LoggerFactory.getLogger("example")

fun main() {
    // create a Database instance
    val db = Database.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = AddressDef.meta

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

        logger.info("address1 = $address1")

        // UPDATE
        db.runQuery {
            EntityDsl.update(a).single(address1.copy(street = "street B"))
        }

        // READ: select by street
        val address2 = db.runQuery {
            EntityDsl.from(a).first { a.street eq "street B" }
        }

        logger.info("address2 = $address2")
        check(address1.id == address2.id)
        check(address1.street != address2.street)
        check(address1.version + 1 == address2.version)

        // DELETE
        db.runQuery {
            EntityDsl.delete(a).single(address2)
        }

        // READ: select all
        val addressList = db.runQuery {
            EntityDsl.from(a).orderBy(a.id)
        }

        logger.info("addressList = $addressList")
        check(addressList.isEmpty()) { "The addressList must be empty." }
    }
}
