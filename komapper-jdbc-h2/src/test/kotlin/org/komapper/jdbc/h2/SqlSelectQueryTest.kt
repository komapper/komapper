package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.count
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class SqlSelectQueryTest(private val db: Database) {

    @Test
    fun list() {
        val a = Address.metamodel()
        val list: List<Address> = db.execute {
            SqlQuery.from(a)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun first() {
        val a = Address.metamodel()
        val address: Address = db.execute {
            SqlQuery.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.metamodel()
        val address: Address? = db.execute {
            SqlQuery.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun transform() {
        val a = Address.metamodel()
        val count = db.execute {
            SqlQuery.from(a).transform { it.count() }
        }
        assertEquals(15, count)
    }

    @Test
    fun option() {
        val e = Employee.metamodel()
        val emp = db.execute {
            SqlQuery.from(e)
                .option {
                    fetchSize = 10
                    maxRows = 100
                    queryTimeoutSeconds = 1000
                    allowEmptyWhereClause = true
                }
                .where {
                    e.employeeId eq 1
                }.first()
        }
        println(emp)
    }
}
