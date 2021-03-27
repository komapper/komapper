package example

import org.intellij.lang.annotations.Language
import org.komapper.core.Database
import org.komapper.core.EntityQuery
import org.komapper.core.KmColumn
import org.komapper.core.KmEntity
import org.komapper.core.KmId
import org.komapper.core.KmIdentityGenerator
import org.komapper.core.KmVersion
import org.komapper.core.TemplateQuery
import org.komapper.jdbc.h2.H2DatabaseConfig

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
        val foundB2 = db.execute {
            data class Params(val street: String)

            @Language("sql")
            val sql = "select ADDRESS_ID, STREET, VERSION from Address where street = /*street*/'test'"
            TemplateQuery.select(sql, Params("street B")) {
                Address(asInt("ADDRESS_ID"), asString("STREET"), asInt("VERSION"))
            }.first()
        }
        check(addressB == foundB2)

        // DELETE
        db.delete(a, addressB)

        // READ: select all
        val addressList = db.execute(EntityQuery.from(a).orderBy(a.id))
        check(addressList.isEmpty())
    }
}

fun check(value: Boolean) {
    if (!value) error("failed.")
}
