package org.komapper.core.template.sql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.core.data.Value
import org.komapper.core.template.expr.DefaultExprEnvironment
import org.komapper.core.template.expr.DefaultExprEvaluator
import org.komapper.core.template.expr.NoCacheExprNodeFactory

class DefaultStatementBuilderTest {

    private val sqlBuilder = DefaultStatementBuilder(
        { value, _ -> if (value is CharSequence) "'$value'" else value.toString() },
        sqlNodeFactory = NoCacheSqlNodeFactory(),
        exprEvaluator = DefaultExprEvaluator(
            NoCacheExprNodeFactory(),
            DefaultExprEnvironment { it }
        )
    )

    @Test
    fun simple() {
        val template = "select * from person"
        val sql = sqlBuilder.build(template)
        assertEquals(template, sql.sql)
    }

    @Test
    fun complex() {
        val template =
            "select name, age from person where name = /*name*/'test' and age > 1 order by name, age for update"
        val sql = sqlBuilder.build(template)
        assertEquals("select name, age from person where name = ? and age > 1 order by name, age for update", sql.sql)
    }

    @Test
    fun `The expression evaluation was failed`() {
        val template =
            "select name, age from person where name = /*name.a*/'test'"
        val exception = assertThrows<SqlException> { sqlBuilder.build(template) }
        println(exception)
    }

    @Nested
    inner class BindValueTest {

        @Test
        fun singleValue() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value("aaa")))
            assertEquals("select name, age from person where name = ? and age > 1", sql.sql)
            assertEquals(listOf(Value("aaa")), sql.values)
        }

        @Test
        fun singleValue_null() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person where name = ? and age > 1", sql.sql)
            assertEquals(listOf(Value(null, Any::class)), sql.values)
        }

        @Test
        fun multipleValues() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value(listOf("x", "y", "z"))))
            assertEquals("select name, age from person where name in (?, ?, ?) and age > 1", sql.sql)
            assertEquals(
                listOf(
                    Value("x"),
                    Value("y"),
                    Value("z")
                ),
                sql.values
            )
        }

        @Test
        fun multipleValues_null() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person where name in ? and age > 1", sql.sql)
            assertEquals(listOf(Value(null, Any::class)), sql.values)
        }

        @Test
        fun multipleValues_empty() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value(emptyList<String>())))
            assertEquals("select name, age from person where name in (null) and age > 1", sql.sql)
            assertTrue(sql.values.isEmpty())
        }

        @Test
        fun pairValues() {
            val sql = sqlBuilder.build(
                "select name, age from person where (name, age) in /*pairs*/(('a', 'b'), ('c', 'd'))",
                mapOf("pairs" to Value(listOf("x" to 1, "y" to 2, "z" to 3)))
            )
            assertEquals("select name, age from person where (name, age) in ((?, ?), (?, ?), (?, ?))", sql.sql)
            assertEquals(
                listOf(
                    Value("x"), Value(1),
                    Value("y"), Value(2),
                    Value("z"), Value(3)
                ),
                sql.values
            )
        }

        @Test
        fun tripleValues() {
            val sql = sqlBuilder.build(
                "select name, age from person where (name, age, weight) in /*triples*/(('a', 'b', 'c'), ('d', 'e', 'f'))",
                mapOf(
                    "triples" to Value(
                        listOf(
                            Triple("x", 1, 10),
                            Triple("y", 2, 20),
                            Triple("z", 3, 30)
                        )
                    )
                )
            )
            assertEquals(
                "select name, age from person where (name, age, weight) in ((?, ?, ?), (?, ?, ?), (?, ?, ?))",
                sql.sql
            )
            assertEquals(
                listOf(
                    Value("x"), Value(1), Value(10),
                    Value("y"), Value(2), Value(20),
                    Value("z"), Value(3), Value(30)
                ),
                sql.values
            )
        }
    }

    @Nested
    inner class EmbeddedValueTest {

        @Test
        fun `Include the 'order by' clause into the embedded value`() {
            val template = "select name, age from person where age > 1 /*# orderBy */"
            val sql = sqlBuilder.build(template, mapOf("orderBy" to Value("order by name")))
            assertEquals("select name, age from person where age > 1 order by name", sql.sql)
        }

        @Test
        fun `Exclude the 'order by' clause from the embedded value`() {
            val template = "select name, age from person where age > 1 order by /*# orderBy */"
            val sql = sqlBuilder.build(template, mapOf("orderBy" to Value("name, age")))
            assertEquals("select name, age from person where age > 1 order by name, age", sql.sql)
        }

        @Test
        fun `Remove the 'order by' clause automatically`() {
            val template = "select name, age from person where age > 1 order by /*# orderBy */"
            val sql = sqlBuilder.build(template, mapOf("orderBy" to Value("")))
            assertEquals("select name, age from person where age > 1 ", sql.sql)
        }
    }

    @Nested
    inner class LiteralValueTest {

        @Test
        fun test() {
            val template = "select name, age from person where name = /*^name*/'test' and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value("aaa")))
            assertEquals("select name, age from person where name = 'aaa' and age > 1", sql.sql)
        }
    }

    @Nested
    inner class IfBlockTest {
        @Test
        fun if_true() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value("aaa")))
            assertEquals("select name, age from person where name = ? and 1 = 1", sql.sql)
            assertEquals(listOf(Value("aaa")), sql.values)
        }

        @Test
        fun if_false() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person where   1 = 1", sql.sql)
        }

        @Test
        fun `Remove the 'where' clause automatically`() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ order by name"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person   order by name", sql.sql)
        }

        @Test
        fun `Remove the 'where' and 'order by' clauses automatically`() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ order by /*%if false*/name/*%end*/"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person ", sql.sql)
        }
    }

    @Nested
    inner class ForBlockTest {
        @Test
        fun test() {
            val template =
                "select name, age from person where /*%for i in list*/age = /*i*/0 /*%if i_has_next *//*# \"or\" */ /*%end*//*%end*/"
            val sql = sqlBuilder.build(template, mapOf("list" to Value(listOf(1, 2, 3))))
            assertEquals("select name, age from person where age = ? or age = ? or age = ? ", sql.sql)
            assertEquals(
                listOf(
                    Value(1),
                    Value(2),
                    Value(3)
                ),
                sql.values
            )
        }
    }

    @Nested
    inner class ParenTest {
        @Test
        fun subQuery() {
            val template = "select name, age from (select * from person)"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from (select * from person)", sql.sql)
        }

        @Test
        fun emptyParentheses() {
            val template = "select name, age from my_function()"
            val sql = sqlBuilder.build(template)
            assertEquals(template, sql.sql)
        }
    }

    @Nested
    inner class SetTest {
        @Test
        fun test() {
            val template = "select name from a union select name from b"
            val sql = sqlBuilder.build(template)
            assertEquals(template, sql.sql)
        }

        @Test
        fun `Remove the 'union' keyword automatically`() {
            val template = "select name from a union /*%if false*/select name from b/*%end*/"
            val sql = sqlBuilder.build(template)
            assertEquals("select name from a ", sql.sql)
        }
    }
}
