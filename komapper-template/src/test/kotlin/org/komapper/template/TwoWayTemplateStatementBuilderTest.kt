package org.komapper.template

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.core.Value
import org.komapper.template.expression.DefaultExprEnvironment
import org.komapper.template.expression.DefaultExprEvaluator
import org.komapper.template.expression.ExprContext
import org.komapper.template.expression.NoCacheExprNodeFactory
import org.komapper.template.sql.NoCacheSqlNodeFactory
import org.komapper.template.sql.SqlException

class TwoWayTemplateStatementBuilderTest {

    private val statementBuilder = TwoWayTemplateStatementBuilder(
        { value, _ -> if (value is CharSequence) "'$value'" else value.toString() },
        sqlNodeFactory = NoCacheSqlNodeFactory(),
        exprEvaluator = DefaultExprEvaluator(
            NoCacheExprNodeFactory(),
            DefaultExprEnvironment()
        )
    )

    @Test
    fun simple() {
        val template = "select * from person"
        val statement = statementBuilder.build(template)
        assertEquals(template, statement.toSql())
    }

    @Test
    fun complex() {
        val template =
            "select name, age from person where name = /*name*/'test' and age > 1 order by name, age for update"
        val statement = statementBuilder.build(template)
        assertEquals("select name, age from person where name = ? and age > 1 order by name, age for update", statement.toSql())
    }

    @Test
    fun `The expression evaluation was failed`() {
        val template =
            "select name, age from person where name = /*name.a*/'test'"
        val exception = assertThrows<SqlException> { statementBuilder.build(template) }
        println(exception)
    }

    @Nested
    inner class BindValueTest {

        @Test
        fun singleValue() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val statement = statementBuilder.build(template, ExprContext(mapOf("name" to Value("aaa"))))
            assertEquals("select name, age from person where name = ? and age > 1", statement.toSql())
            assertEquals(listOf(Value("aaa")), statement.args)
        }

        @Test
        fun singleValue_null() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val statement = statementBuilder.build(template)
            assertEquals("select name, age from person where name = ? and age > 1", statement.toSql())
            assertEquals(listOf(Value(null, Any::class)), statement.args)
        }

        @Test
        fun multipleValues() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val statement = statementBuilder.build(template, ExprContext(mapOf("name" to Value(listOf("x", "y", "z")))))
            assertEquals("select name, age from person where name in (?, ?, ?) and age > 1", statement.toSql())
            assertEquals(
                listOf(
                    Value("x"),
                    Value("y"),
                    Value("z")
                ),
                statement.args
            )
        }

        @Test
        fun multipleValues_null() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val statement = statementBuilder.build(template)
            assertEquals("select name, age from person where name in ? and age > 1", statement.toSql())
            assertEquals(listOf(Value(null, Any::class)), statement.args)
        }

        @Test
        fun multipleValues_empty() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val statement = statementBuilder.build(template, ExprContext(mapOf("name" to Value(emptyList<String>()))))
            assertEquals("select name, age from person where name in (null) and age > 1", statement.toSql())
            assertTrue(statement.args.isEmpty())
        }

        @Test
        fun pairValues() {
            val statement = statementBuilder.build(
                "select name, age from person where (name, age) in /*pairs*/(('a', 'b'), ('c', 'd'))",
                ExprContext(mapOf("pairs" to Value(listOf("x" to 1, "y" to 2, "z" to 3))))
            )
            assertEquals("select name, age from person where (name, age) in ((?, ?), (?, ?), (?, ?))", statement.toSql())
            assertEquals(
                listOf(
                    Value("x"), Value(1),
                    Value("y"), Value(2),
                    Value("z"), Value(3)
                ),
                statement.args
            )
        }

        @Test
        fun tripleValues() {
            val statement = statementBuilder.build(
                "select name, age from person where (name, age, weight) in /*triples*/(('a', 'b', 'c'), ('d', 'e', 'f'))",
                ExprContext(
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
            )
            assertEquals(
                "select name, age from person where (name, age, weight) in ((?, ?, ?), (?, ?, ?), (?, ?, ?))",
                statement.toSql()
            )
            assertEquals(
                listOf(
                    Value("x"), Value(1), Value(10),
                    Value("y"), Value(2), Value(20),
                    Value("z"), Value(3), Value(30)
                ),
                statement.args
            )
        }
    }

    @Nested
    inner class EmbeddedValueTest {

        @Test
        fun `Include the 'order by' clause into the embedded value`() {
            val template = "select name, age from person where age > 1 /*# orderBy */"
            val statement = statementBuilder.build(template, ExprContext(mapOf("orderBy" to Value("order by name"))))
            assertEquals("select name, age from person where age > 1 order by name", statement.toSql())
        }

        @Test
        fun `Exclude the 'order by' clause from the embedded value`() {
            val template = "select name, age from person where age > 1 order by /*# orderBy */"
            val statement = statementBuilder.build(template, ExprContext(mapOf("orderBy" to Value("name, age"))))
            assertEquals("select name, age from person where age > 1 order by name, age", statement.toSql())
        }

        @Test
        fun `Remove the 'order by' clause automatically`() {
            val template = "select name, age from person where age > 1 order by /*# orderBy */"
            val statement = statementBuilder.build(template, ExprContext(mapOf("orderBy" to Value(""))))
            assertEquals("select name, age from person where age > 1 ", statement.toSql())
        }
    }

    @Nested
    inner class LiteralValueTest {

        @Test
        fun test() {
            val template = "select name, age from person where name = /*^name*/'test' and age > 1"
            val statement = statementBuilder.build(template, ExprContext(mapOf("name" to Value("aaa"))))
            assertEquals("select name, age from person where name = 'aaa' and age > 1", statement.toSql())
        }
    }

    @Nested
    inner class IfBlockTest {
        @Test
        fun if_true() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val statement = statementBuilder.build(template, ExprContext(mapOf("name" to Value("aaa"))))
            assertEquals("select name, age from person where name = ? and 1 = 1", statement.toSql())
            assertEquals(listOf(Value("aaa")), statement.args)
        }

        @Test
        fun if_false() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val statement = statementBuilder.build(template)
            assertEquals("select name, age from person where   1 = 1", statement.toSql())
        }

        @Test
        fun `Remove the 'where' clause automatically`() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ order by name"
            val statement = statementBuilder.build(template)
            assertEquals("select name, age from person   order by name", statement.toSql())
        }

        @Test
        fun `Remove the 'where' and 'order by' clauses automatically`() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ order by /*%if false*/name/*%end*/"
            val statement = statementBuilder.build(template)
            assertEquals("select name, age from person ", statement.toSql())
        }
    }

    @Nested
    inner class ForBlockTest {
        @Test
        fun test() {
            val template =
                "select name, age from person where /*%for i in list*/age = /*i*/0 /*%if i_has_next *//*# \"or\" */ /*%end*//*%end*/"
            val statement = statementBuilder.build(template, ExprContext(mapOf("list" to Value(listOf(1, 2, 3)))))
            assertEquals("select name, age from person where age = ? or age = ? or age = ? ", statement.toSql())
            assertEquals(
                listOf(
                    Value(1),
                    Value(2),
                    Value(3)
                ),
                statement.args
            )
        }
    }

    @Nested
    inner class ParenTest {
        @Test
        fun subQuery() {
            val template = "select name, age from (select * from person)"
            val statement = statementBuilder.build(template)
            assertEquals("select name, age from (select * from person)", statement.toSql())
        }

        @Test
        fun emptyParentheses() {
            val template = "select name, age from my_function()"
            val statement = statementBuilder.build(template)
            assertEquals(template, statement.toSql())
        }
    }

    @Nested
    inner class SetTest {
        @Test
        fun test() {
            val template = "select name from a union select name from b"
            val statement = statementBuilder.build(template)
            assertEquals(template, statement.toSql())
        }

        @Test
        fun `Remove the 'union' keyword automatically`() {
            val template = "select name from a union /*%if false*/select name from b/*%end*/"
            val statement = statementBuilder.build(template)
            assertEquals("select name from a ", statement.toSql())
        }
    }
}
