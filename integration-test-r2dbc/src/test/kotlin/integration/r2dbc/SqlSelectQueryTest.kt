package integration.r2dbc

import integration.Address
import integration.Employee
import integration.meta
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.metamodel.define
import org.komapper.core.dsl.operator.case
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class SqlSelectQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun list() = inTransaction(db) {
        val a = Address.meta
        val list: List<Address> = db.runQuery {
            QueryDsl.from(a)
        }.toList()
        assertEquals(15, list.size)
    }

    @Test
    fun first() = inTransaction(db) {
        val a = Address.meta
        val address: Address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() = inTransaction(db) {
        val a = Address.meta
        val address: Address? = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun decoupling() = inTransaction(db) {
        val a = Address.meta
        val query = QueryDsl.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val flow = db.runQuery { query }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            flow.toList()
        )
    }

    @Test
    fun option() = inTransaction(db) {
        val e = Employee.meta
        val emp = db.runQuery {
            QueryDsl.from(e)
                .options {
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
    fun caseExpression() = inTransaction(db) {
        val a = Address.meta
        val caseExpression = case(
            When(
                { a.street eq "STREET 2"; a.addressId greater 1 },
                literal("HIT")
            )
        ) { literal("NO HIT") }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }.toList()
        assertEquals(
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
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }.toList()
        assertEquals(
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
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2, 3) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }.toList()
        assertEquals(
            listOf("STREET 1" to null, "STREET 2" to "HIT", "STREET 3" to null),
            list
        )
    }

    @Test
    fun defaultWhere() = inTransaction(db) {
        val a = Address.meta.define { a ->
            where { a.addressId eq 1 }
        }
        val list = db.runQuery { QueryDsl.from(a) }
        assertEquals(1, list.size)
    }

    @Test
    fun defaultWhere_update_single() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        val a2 = Address.meta.define { a2 ->
            where { a2.version eq 99 }
        }
        assertThrows<OptimisticLockException> {
            runBlocking {
                db.runQuery { QueryDsl.update(a2).single(address) }.run { }
            }
        }
    }

    @Test
    fun defaultWhere_update_set() = inTransaction(db) {
        val a = Address.meta.define { a ->
            where { a.addressId eq 15 }
        }
        val count = db.runQuery { QueryDsl.update(a).set { a.street set "hello" } }
        assertEquals(1, count)
    }

    @Test
    fun defaultWhere_delete_single() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        val a2 = Address.meta.define { a2 ->
            where { a2.version eq 99 }
        }
        assertThrows<OptimisticLockException> {
            runBlocking {
                db.runQuery { QueryDsl.delete(a2).single(address) }
            }
        }
    }

    @Test
    fun defaultWhere_delete_all() = inTransaction(db) {
        val a = Address.meta.define { a ->
            where { a.addressId eq 15 }
        }
        val count = db.runQuery { QueryDsl.delete(a).all() }
        assertEquals(1, count)
    }
}
