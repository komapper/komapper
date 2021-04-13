package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.execute
import org.komapper.core.dsl.plus

@ExtendWith(Env::class)
class EntityMergeQueryTest(private val db: Database) {

    @Test
    fun insert() {
        val d = Department.metamodel()
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = H2EntityQuery.merge(d, department)
        val (count, keys) = db.execute { query }
        assertEquals(1, count)
        assertEquals(0, keys.size)
        val found = db.execute { EntityQuery.first(d) { d.departmentId eq 5 } }
        assertNotNull(found)
    }

    @Test
    fun insert_on() {
        val d = Department.metamodel()
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = H2EntityQuery.merge(d, department).on(d.departmentName)
        val (count, keys) = db.execute { query }
        assertEquals(1, count)
        assertEquals(0, keys.size)
        val found = db.execute { EntityQuery.first(d) { d.departmentId eq 5 } }
        assertNotNull(found)
    }

    @Test
    fun update() {
        val d = Department.metamodel()
        val department = Department(1, 50, "PLANNING", "TOKYO", 1)
        val query = H2EntityQuery.merge(d, department)
        val (count, keys) = db.execute { query }
        assertEquals(1, count)
        assertEquals(0, keys.size)
        val found = db.execute { EntityQuery.first(d) { d.departmentId eq 1 } }
        assertEquals(50, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(2, found.version)
    }

    @Test
    fun update_on() {
        val d = Department.metamodel()
        val department = Department(5, 10, "PLANNING", "TOKYO", 1)
        val query = H2EntityQuery.merge(d, department).on(d.departmentNo)
        val (count, keys) = db.execute { query }
        assertEquals(1, count)
        assertEquals(0, keys.size)
        val found = db.execute { EntityQuery.first(d) { d.departmentNo eq 10 } }
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
    }

    @Test
    fun optimisticLockException() {
        val d = Department.metamodel()
        db.execute {
            SqlQuery.update(d).set { d.version set d.version + 10 }.where { d.departmentId eq 1 }
        }
        val department = Department(1, 50, "PLANNING", "TOKYO", 1)
        assertThrows<OptimisticLockException> {
            db.execute {
                H2EntityQuery.merge(d, department)
            }
            Unit
        }
    }

    @Test
    fun uniqueConstraintException() {
        val d = Department.metamodel()
        val department = db.execute { EntityQuery.first(d) { d.departmentId eq 1 } }
        assertThrows<UniqueConstraintException> {
            val department2 = department.copy(departmentId = 5)
            db.execute { H2EntityQuery.merge(d, department2) }.let { }
        }
    }

    @Test
    fun option_ignoreVersion() {
        val d = Department.metamodel()
        db.execute {
            SqlQuery.update(d).set { d.version set d.version + 10 }.where { d.departmentId eq 1 }
        }
        db.execute {
            val department = Department(1, 50, "PLANNING", "TOKYO", 1)
            H2EntityQuery.merge(d, department).option { it.copy(ignoreVersion = true) }
        }
        val department = db.execute {
            EntityQuery.first(d) { d.departmentId eq 1 }
        }
        assertEquals(2, department.version)
    }
}
