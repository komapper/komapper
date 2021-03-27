package org.komapper.core.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.EntityQuery

class QueryTest {

    @Test
    fun query_objects_are_immutable() {
        val a = Address.metamodel()

        val q1 = EntityQuery.from(a).where() {
            a.id eq 1
        }
        val q2 = q1.where {
            a.street eq "a"
        }
        val q3 = q2.orderBy(a.version)

        val s1 = "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID = ?"
        val s2 = "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID = ? and t0_.STREET = ?"
        val s3 =
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID = ? and t0_.STREET = ? order by t0_.VERSION asc"

        assertEquals(s1, q1.peek().sql)
        assertEquals(s2, q2.peek().sql)
        assertEquals(s3, q3.peek().sql)
    }
}
