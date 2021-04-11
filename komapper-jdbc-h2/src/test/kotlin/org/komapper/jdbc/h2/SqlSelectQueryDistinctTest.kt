package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class SqlSelectQueryDistinctTest(private val db: Database) {

    @Test
    fun distinct() {
        val d = Department.metamodel()
        val e = Employee.metamodel()
        val query = SqlQuery.from(d).innerJoin(e) { d.departmentId eq e.departmentId }
        val list = db.execute { query }
        assertEquals(14, list.size)

        val query2 = query.distinct()
        val list2 = db.execute { query2 }
        assertEquals(3, list2.size)
    }
}
