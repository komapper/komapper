package integration.jdbc

import integration.Address
import integration.Employee
import integration.meta
import kotlinx.coroutines.flow.count
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.case
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.literal
import org.komapper.jdbc.Database

@ExtendWith(Env::class)
class SqlSelectQueryTest(private val db: Database) {

    @Test
    fun list() {
        val a = Address.meta
        val list: List<Address> = db.runQuery {
            SqlDsl.from(a)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun first() {
        val a = Address.meta
        val address: Address = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.meta
        val address: Address? = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun collect() {
        val a = Address.meta
        val count = db.runQuery {
            SqlDsl.from(a).collect { it.count() }
        }
        assertEquals(15, count)
    }

    @Test
    fun option() {
        val e = Employee.meta
        val emp = db.runQuery {
            SqlDsl.from(e)
                .option {
                    it.copy(
                        fetchSize = 10,
                        maxRows = 100,
                        queryTimeoutSeconds = 1000,
                        allowEmptyWhereClause = true,
                    )
                }
                .where {
                    e.employeeId eq 1
                }.first()
        }
        println(emp)
    }

    @Test
    fun shortcut_first() {
        val a = Address.meta
        val address = db.runQuery { SqlDsl.from(a).where { a.addressId eq 1 }.first() }
        assertNotNull(address)
    }

    @Test
    fun shortcut_firstOrNull() {
        val a = Address.meta
        val address = db.runQuery { SqlDsl.from(a).where { a.addressId eq -1 }.firstOrNull() }
        assertNull(address)
    }

    @Test
    fun shortcut_first_multipleCondition() {
        val a = Address.meta
        val address = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 1; a.version eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun caseExpression() {
        val a = Address.meta
        val caseExpression = case(
            When(
                { a.street eq "STREET 2"; a.addressId greater 1 },
                literal("HIT")
            )
        ) { literal("NO HIT") }
        val list = db.runQuery {
            SqlDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "NO HIT"),
            list
        )
    }

    @Test
    fun caseExpression_multipleWhen() {
        val a = Address.meta
        val caseExpression = case(
            When(
                { a.street eq "STREET 2"; a.addressId greater 1 },
                literal("HIT")
            ),
            When(
                { a.street eq "STREET 3" },
                concat(a.street, "!!!")
            )
        ) { literal("NO HIT") }
        val list = db.runQuery {
            SqlDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "STREET 3!!!"),
            list
        )
    }

    @Test
    fun caseExpression_otherwiseNotSpecified() {
        val a = Address.meta
        val caseExpression = case(
            When(
                { a.street eq "STREET 2"; a.addressId greater 1 },
                literal("HIT")
            )
        )
        val list = db.runQuery {
            SqlDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf("STREET 1" to null, "STREET 2" to "HIT", "STREET 3" to null),
            list
        )
    }
}
