package org.komapper.core.template.sql

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlReassemblerTest {

    @Test
    fun test() {
        val sql = """
            /**
             * multi line comment
             */
            -- single line comment
            select
                *
            from
                dept
            where
                /*%for id in list */
                id = /* id */1
                /*%if id_has_next *//*# "or" *//*%end */
                /*%end */
            union all
            select
                * 
            from
                emp 
            where 
                id = /* id */1
                and
                name = /*^ name */'foo'
            /*%if orderBy != null*/
            /*# orderBy */
            /*%else */
            order by id
            /*%end */
        """.trimIndent()
        val newSql = SqlReassembler(sql, emptyMap()).assemble()
        assertEquals(sql, newSql)
    }

    @Test
    fun partialFound() {
        val sql = """
            select
                *
            from
                dept
            /*> orderBy */
        """.trimIndent()
        val newSql = SqlReassembler(sql, mapOf("orderBy" to "order by id, name")).assemble()
        assertEquals(
            newSql,
            """
            select
                *
            from
                dept
            order by id, name
            """.trimIndent(),
        )
    }

    @Test
    fun partialNotFound() {
        val sql = """
            select
                *
            from
                dept
            /*> orderBy */
        """.trimIndent()
        val e = assertThrows<SqlReassembler.SqlPartialNotFoundException> {
            SqlReassembler(sql, emptyMap()).assemble()
        }
        assertEquals("orderBy", e.name)
    }
}
