package integration.jdbc

import integration.address
import integration.employee
import integration.identityStrategy
import integration.person
import integration.sequenceStrategy
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class InsertValuesTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Meta.address
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
    fun setIfNotNull() {
        val e = Meta.employee
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
    fun generatedKeys_autoIncrement() {
        val a = Meta.identityStrategy
        val (count, id) = db.runQuery {
            QueryDsl.insert(a).values {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertIs<Int>(id)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun generatedKeys_sequence() {
        val a = Meta.sequenceStrategy
        val (count, id) = db.runQuery {
            QueryDsl.insert(a).values {
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertIs<Int>(id)
    }

    @Test
    fun timestamp() {
        val p = Meta.person
        val (count) = db.runQuery {
            QueryDsl.insert(p).values {
                p.personId set 99
                p.name set "test"
            }
        }
        assertEquals(1, count)
        val person = db.runQuery {
            QueryDsl.from(p).where { p.personId eq 99 }.first()
        }
        assertNotNull(person.createdAt)
        assertNotNull(person.updatedAt)
    }

    @Test
    fun select() {
        val a = Meta.address
        val aa = a.clone(table = "ADDRESS_ARCHIVE")
        val (count) = db.runQuery {
            QueryDsl.insert(aa).select {
                QueryDsl.from(a).where { a.addressId between 1..5 }
            }
        }
        assertEquals(5, count)
    }
}
