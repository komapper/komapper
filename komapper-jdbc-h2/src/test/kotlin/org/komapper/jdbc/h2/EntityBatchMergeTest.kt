package org.komapper.jdbc.h2

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database

@ExtendWith(Env::class)
class EntityBatchMergeTest(private val db: Database) {
/*
    @Test
    fun keys() {
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 0),
            Department(6, 10, "DEVELOPMENT", "KYOTO", 0)
        )
        db.batchMerge(departments, Department::departmentNo)
        Assertions.assertEquals(
            departments[0],
            db.findById<Department>(5)
        )
        Assertions.assertNull(
            db.findById<Department>(
                6
            )
        )
        Assertions.assertEquals(
            departments[1].copy(departmentId = 1),
            db.findById<Department>(1)
        )
    }

    @Test
    fun noKeys() {
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 0),
            Department(1, 60, "DEVELOPMENT", "KYOTO", 0)
        )
        db.batchMerge(departments)
        Assertions.assertEquals(
            departments[0],
            db.findById<Department>(5)
        )
        Assertions.assertEquals(
            departments[1],
            db.findById<Department>(1)
        )
    }

    @Test
    fun uniqueConstraintException() {
        val department = db.findById<Department>(1)!!
        assertThrows<UniqueConstraintException> {
            db.batchMerge(
                listOf(department.copy(departmentId = 2))
            )
        }
    }
    
 */
}
