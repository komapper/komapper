package integration.jdbc

import integration.Address
import integration.Employee
import integration.IdentityStrategy
import integration.meta
import integration.newMeta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class SqlInsertQueryTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Address.meta
        val (count, key) = db.runQuery {
            SqlDsl.insert(a).values {

                a.addressId set 19
                a.street set "STREET 16"
                a.version set 0
            }
        }
        assertEquals(1, count)
        assertNull(key)
    }

    @Test
    fun setIfNotNull() {
        val e = Employee.meta
        val (count, key) = db.runQuery {
            SqlDsl.insert(e).values {
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

        val employee = db.runQuery { SqlDsl.from(e).where { e.employeeId eq 99 }.first() }
        assertNull(employee.managerId)
    }

    @Test
    fun generatedKeys() {
        val a = IdentityStrategy.meta
        val (count, key) = db.runQuery {
            SqlDsl.insert(a).values {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertNotNull(key)
    }

    @Test
    fun select() {
        val a = Address.meta
        val aa = Address.newMeta(table = "ADDRESS_ARCHIVE")
        val (count) = db.runQuery {
            SqlDsl.insert(aa).select {
                SqlDsl.from(a).where { a.addressId between 1..5 }
            }
        }
        assertEquals(5, count)
    }
}
