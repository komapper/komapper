package integration.r2dbc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.case
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.literal
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class SqlSelectQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun list() = inTransaction(db) {
        val a = Address.meta
        val list: List<Address> = db.runQuery {
            SqlDsl.from(a)
        }.toList()
        Assertions.assertEquals(15, list.size)
    }

    @Test
    fun first() = inTransaction(db) {
        val a = Address.meta
        val address: Address = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 1 }.first()
        }
        Assertions.assertNotNull(address)
    }

    @Test
    fun firstOrNull() = inTransaction(db) {
        val a = Address.meta
        val address: Address? = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        Assertions.assertNull(address)
    }

    @Test
    fun option() = inTransaction(db) {
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
    fun shortcut_first() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery { SqlDsl.from(a).where { a.addressId eq 1 }.first() }
        Assertions.assertNotNull(address)
    }

    @Test
    fun shortcut_firstOrNull() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery { SqlDsl.from(a).where { a.addressId eq -1 }.firstOrNull() }
        Assertions.assertNull(address)
    }

    @Test
    fun shortcut_first_multipleCondition() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 1; a.version eq 1 }.first()
        }
        Assertions.assertNotNull(address)
    }

    @Test
    fun caseExpression() = inTransaction(db) {
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
        }.toList()
        Assertions.assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "NO HIT"),
            list
        )
    }

    @Test
    fun caseExpression_multipleWhen() = inTransaction(db) {
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
        }.toList()
        Assertions.assertEquals(
            listOf("STREET 1" to "NO HIT", "STREET 2" to "HIT", "STREET 3" to "STREET 3!!!"),
            list
        )
    }

    @Test
    fun caseExpression_otherwiseNotSpecified() = inTransaction(db) {
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
        }.toList()
        Assertions.assertEquals(
            listOf("STREET 1" to null, "STREET 2" to "HIT", "STREET 3" to null),
            list
        )
    }
}
