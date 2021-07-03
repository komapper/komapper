package example

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.SchemaDsl
import org.komapper.jdbc.JdbcDatabase
import org.komapper.tx.jdbc.withTransaction
import java.time.LocalDateTime

data class Address(
    val id: Int = 0,
    val street: String,
    val version: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

@KomapperEntityDef(Address::class)
data class AddressDef(
    @KomapperId @KomapperAutoIncrement @KomapperColumn(name = "ADDRESS_ID")
    val id: Nothing,
    @KomapperVersion val version: Nothing,
    @KomapperCreatedAt val createdAt: Nothing,
    @KomapperUpdatedAt val updatedAt: Nothing,
) {
    companion object
}

fun main() {
    // create a Database instance
    val db = JdbcDatabase.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    // get a metamodel
    val a = AddressDef.meta

    // execute simple CRUD operations as a transaction
    db.withTransaction {
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
            EntityDsl.from(a).where { a.id eq newAddress.id }.first()
        }

        println("address1 = $address1")

        // UPDATE
        db.runQuery {
            EntityDsl.update(a).single(address1.copy(street = "street B"))
        }

        // READ: select by street
        val address2 = db.runQuery {
            EntityDsl.from(a).where { a.street eq "street B" }.first()
        }

        println("address2 = $address2")
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

        println("addressList = $addressList")
        check(addressList.isEmpty()) { "The addressList must be empty." }
    }
}
