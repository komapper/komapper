package org.komapper.core.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JoinTest {

    @Test
    fun association_invalid_e1() {
        val a = Address.metamodel()
        val e = Emp.metamodel()
        val query = EntityQuery.from(a).innerJoin(a) { a.id eq a.id }
        val ex = assertThrows<IllegalStateException> {
            query.associate(e, a) { emp, _ ->
                emp
            }
        }
        assertEquals("The e1 is not found. Use e1 in the join clause.", ex.message)
    }

    @Test
    fun association_invalid_e2() {
        val a = Address.metamodel()
        val e = Emp.metamodel()
        val query = EntityQuery.from(a).innerJoin(a) { a.id eq a.id }
        val ex = assertThrows<IllegalStateException> {
            query.associate(a, e) { address, _ ->
                address
            }
        }
        assertEquals("The e2 is not found. Use e2 in the join clause.", ex.message)
    }
}
