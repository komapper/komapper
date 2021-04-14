package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.execute
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(Env::class)
class SqlInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val (count, keys) = db.execute {
            SqlQuery.insert(a).values {
                a.addressId set 19
                a.street set "STREET 16"
                a.version set 0
            }
        }
        assertEquals(1, count)
        assertEquals(0, keys.size)
    }

    @Test
    fun setIfNotNull() {
        val e = Employee.alias
        val (count, keys) = db.execute {
            SqlQuery.insert(e).values {
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
        assertEquals(0, keys.size)

        val employee = db.execute { SqlQuery.first(e) { e.employeeId eq 99 } }
        assertNull(employee.managerId)
    }

    @Test
    fun generatedKeys() {
        val a = IdentityStrategy.alias
        val (count, keys) = db.execute {
            SqlQuery.insert(a).values {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertEquals(1, keys.size)
    }

    @Test
    fun select() {
        val a = Address.alias
        val aa = Address.newAlias(table = "ADDRESS_ARCHIVE")
        val (count) = db.execute {
            SqlQuery.insert(aa).select {
                SqlQuery.from(a).where { a.addressId between 1..5 }
            }
        }
        assertEquals(5, count)
    }
}
