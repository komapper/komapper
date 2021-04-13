package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class EntityBatchMergeTest(private val db: Database) {

    @Test
    fun batchMerge() {
        val d = Department.alias
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(6, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = H2EntityQuery.batchMerge(d, listOf(department1, department2)).on(d.departmentNo)
        val (counts, keys) = db.execute { query }
        assertEquals(2, counts.size)
        assertEquals(1, counts[0])
        assertEquals(1, counts[1])
        assertEquals(2, keys.size)
        assertEquals(0, keys[0])
        assertEquals(0, keys[1])
        val list = db.execute {
            EntityQuery.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(listOf(1 to "KYOTO", 5 to "TOKYO"), list.map { it.departmentId to it.location })
        println(list)
    }
}
