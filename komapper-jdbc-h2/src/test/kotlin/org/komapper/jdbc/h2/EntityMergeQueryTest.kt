package org.komapper.jdbc.h2

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database

@ExtendWith(Env::class)
class EntityMergeQueryTest(private val db: Database) {

    /*
    @Test
    fun insert_keys() {
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        db.merge(department, Department::departmentNo)
        val department2 = db.findById<Department>(5)
        Assertions.assertEquals(department, department2)
    }

    @Test
    fun insert_noKeys() {
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        db.merge(department)
        val department2 = db.findById<Department>(5)
        Assertions.assertEquals(department, department2)
    }

    @Test
    fun update_keys() {
        val department = Department(5, 10, "PLANNING", "TOKYO", 0)
        db.merge(department, Department::departmentNo)
        Assertions.assertNull(
            db.findById<Department>(
                5
            )
        )
        Assertions.assertEquals(
            department.copy(departmentId = 1),
            db.findById<Department>(1)
        )
    }

    @Test
    fun update_noKeys() {
        val department = Department(1, 50, "PLANNING", "TOKYO", 0)
        db.merge(department)
        val department2 = db.findById<Department>(1)
        Assertions.assertEquals(department, department2)
    }

    @Test
    fun uniqueConstraintException() {
        val department = db.findById<Department>(1)!!
        assertThrows<UniqueConstraintException> {
            db.merge(
                department.copy(departmentId = 2)
            )
        }
    }
     */
}
