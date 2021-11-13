package integration.r2dbc

import integration.Address
import integration.Employee
import integration.IdentityStrategy
import integration.meta
import integration.newMeta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@ExtendWith(Env::class)
class SqlInsertQueryValuesTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val a = Address.meta
        val (count, key) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId set 19
                a.street set "STREET 16"
                a.version set 0
            }
        }
        assertEquals(1, count)
        assertNull(key)
    }

    @Test
    fun setIfNotNull() = inTransaction(db) {
        val e = Employee.meta
        val (count, key) = db.runQuery {
            QueryDsl.insert(e).values {
                e.employeeId set 99
                e.departmentId set 1
                e.addressId set 1
                e.employeeName set "aaa"
                e.employeeNo set 99
                e.hiredate set LocalDate.now()
                e.salary set BigDecimal("1000")
                e.version set 1
                e.managerId setIfNotNull null
            }
        }
        assertEquals(1, count)
        assertNull(key)

        val employee = db.runQuery { QueryDsl.from(e).where { e.employeeId eq 99 }.first() }
        assertNull(employee.managerId)
    }

    @Test
    fun generatedKeys() = inTransaction(db) {
        val a = IdentityStrategy.meta
        val (count, id) = db.runQuery {
            QueryDsl.insert(a).values {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertIs<Int>(id)
    }

    @Test
    fun select() = inTransaction(db) {
        val a = Address.meta
        val aa = Address.newMeta(table = "ADDRESS_ARCHIVE")
        val (count) = db.runQuery {
            QueryDsl.insert(aa).select {
                QueryDsl.from(a).where { a.addressId between 1..5 }
            }
        }
        assertEquals(5, count)
    }
}
