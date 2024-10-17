package org.komapper.template

import org.junit.jupiter.api.Nested
import org.komapper.core.BuilderDialect
import org.komapper.core.DryRunDataOperator
import org.komapper.core.DryRunDialect
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.Value
import org.komapper.core.template.expression.NoCacheExprNodeFactory
import org.komapper.core.template.sql.NoCacheSqlNodeFactory
import org.komapper.core.template.sql.SqlException
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TwoWayTemplateStatementBuilderTest {
    private val statementBuilder = TwoWayTemplateStatementBuilder(
        dialect = BuilderDialect(DryRunDialect, DryRunDataOperator),
        sqlNodeFactory = NoCacheSqlNodeFactory(),
        exprEvaluator = DefaultExprEvaluator(
            NoCacheExprNodeFactory(),
            DefaultExprEnvironment(),
        ),
    )

    private val extensions = TemplateBuiltinExtensions { it }

    @Test
    fun simple() {
        val template = "select * from person"
        val statement = statementBuilder.build(template, emptyMap(), extensions)
        assertEquals(template, statement.toSql())
    }

    @Test
    fun complex() {
        val template =
            "select name, age from person where name = /*name*/'test' and age > 1 order by name, age for update"
        val valueMap = mapOf("name" to Value("", typeOf<String>()))
        val statement = statementBuilder.build(template, valueMap, extensions)
        assertEquals(
            "select name, age from person where name = ? and age > 1 order by name, age for update",
            statement.toSql(),
        )
    }

    @Test
    fun `The expression evaluation failed`() {
        val template =
            "select name, age from person where name = /*name.a*/'test'"
        val exception = assertFailsWith<SqlException> {
            statementBuilder.build(template, emptyMap(), extensions)
        }
        println(exception)
    }

    @Nested
    inner class BindValueTest {
        @Test
        fun singleValue() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val statement = statementBuilder.build(template, mapOf("name" to Value("aaa", typeOf<String>())), extensions)
            assertEquals("select name, age from person where name = ? and age > 1", statement.toSql())
            assertEquals(listOf(Value("aaa", typeOf<String>())), statement.args)
        }

        @Test
        fun singleValue_null() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val statement = statementBuilder.build(template, mapOf("name" to Value(null, typeOf<String>())), extensions)
            assertEquals("select name, age from person where name = ? and age > 1", statement.toSql())
            assertEquals(listOf(Value(null, typeOf<String>())), statement.args)
        }

        @Test
        fun multipleValues() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val statement =
                statementBuilder.build(template, mapOf("name" to Value(listOf("x", "y", "z"), typeOf<List<String>>())), extensions)
            assertEquals("select name, age from person where name in (?, ?, ?) and age > 1", statement.toSql())
            assertEquals(
                listOf(
                    Value("x", typeOf<String>()),
                    Value("y", typeOf<String>()),
                    Value("z", typeOf<String>()),
                ),
                statement.args,
            )
        }

        @Test
        fun multipleValues_null() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val valueMap = mapOf("name" to Value(null, typeOf<Any>()))
            val statement = statementBuilder.build(template, valueMap, extensions)
            assertEquals("select name, age from person where name in ? and age > 1", statement.toSql())
            assertEquals(listOf(Value(null, typeOf<Any>())), statement.args)
        }

        @Test
        fun multipleValues_empty() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val statement =
                statementBuilder.build(template, mapOf("name" to Value(emptyList<String>(), typeOf<List<String>>())), extensions)
            assertEquals("select name, age from person where name in (null) and age > 1", statement.toSql())
            assertTrue(statement.args.isEmpty())
        }

        @Test
        fun pairValues() {
            val statement = statementBuilder.build(
                "select name, age from person where (name, age) in /*pairs*/(('a', 'b'), ('c', 'd'))",
                mapOf("pairs" to Value(listOf("x" to 1, "y" to 2, "z" to 3), typeOf<Pair<String, Int>>())),
                extensions,
            )
            assertEquals(
                "select name, age from person where (name, age) in ((?, ?), (?, ?), (?, ?))",
                statement.toSql(),
            )
            assertEquals(
                listOf(
                    Value("x", typeOf<String>()),
                    Value(1, typeOf<Int>()),
                    Value("y", typeOf<String>()),
                    Value(2, typeOf<Int>()),
                    Value("z", typeOf<String>()),
                    Value(3, typeOf<Int>()),
                ),
                statement.args,
            )
        }

        @Test
        fun tripleValues() {
            val statement = statementBuilder.build(
                "select name, age from person where (name, age, weight) in /*triples*/(('a', 'b', 'c'), ('d', 'e', 'f'))",
                mapOf(
                    "triples" to Value(
                        listOf(
                            Triple("x", 1, 10),
                            Triple("y", 2, 20),
                            Triple("z", 3, 30),
                        ),
                        typeOf<List<Triple<String, Int, Int>>>(),
                    ),
                ),
                extensions,
            )
            assertEquals(
                "select name, age from person where (name, age, weight) in ((?, ?, ?), (?, ?, ?), (?, ?, ?))",
                statement.toSql(),
            )
            assertEquals(
                listOf(
                    Value("x", typeOf<String>()),
                    Value(1, typeOf<Int>()),
                    Value(10, typeOf<Int>()),
                    Value("y", typeOf<String>()),
                    Value(2, typeOf<Int>()),
                    Value(20, typeOf<Int>()),
                    Value("z", typeOf<String>()),
                    Value(3, typeOf<Int>()),
                    Value(30, typeOf<Int>()),
                ),
                statement.args,
            )
        }
    }

    @Nested
    inner class EmbeddedValueTest {
        @Test
        fun `Include the 'order by' clause into the embedded value`() {
            val template = "select name, age from person where age > 1 /*# orderBy */"
            val statement =
                statementBuilder.build(template, mapOf("orderBy" to Value("order by name", typeOf<String>())), extensions)
            assertEquals("select name, age from person where age > 1 order by name", statement.toSql())
        }

        @Test
        fun `Exclude the 'order by' clause from the embedded value`() {
            val template = "select name, age from person where age > 1 order by /*# orderBy */"
            val statement =
                statementBuilder.build(template, mapOf("orderBy" to Value("name, age", typeOf<String>())), extensions)
            assertEquals("select name, age from person where age > 1 order by name, age", statement.toSql())
        }

        @Test
        fun `Remove the 'order by' clause automatically`() {
            val template = "select name, age from person where age > 1 order by /*# orderBy */"
            val statement = statementBuilder.build(template, mapOf("orderBy" to Value("", typeOf<String>())), extensions)
            assertEquals("select name, age from person where age > 1 ", statement.toSql())
        }
    }

    @Nested
    inner class LiteralValueTest {
        @Test
        fun test() {
            val template = "select name, age from person where name = /*^name*/'test' and age > 1"
            val statement = statementBuilder.build(template, mapOf("name" to Value("aaa", typeOf<String>())), extensions)
            assertEquals("select name, age from person where name = 'aaa' and age > 1", statement.toSql())
        }
    }

    @Nested
    inner class IfBlockTest {
        @Test
        fun if_true() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val statement = statementBuilder.build(template, mapOf("name" to Value("aaa", typeOf<String>())), extensions)
            assertEquals("select name, age from person where name = ? and 1 = 1", statement.toSql())
            assertEquals(listOf(Value("aaa", typeOf<String>())), statement.args)
        }

        @Test
        fun if_false() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val statement = statementBuilder.build(template, mapOf("name" to Value(null, typeOf<String>())), extensions)
            assertEquals("select name, age from person where   1 = 1", statement.toSql())
        }

        @Test
        fun `Remove the 'where' clause automatically`() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ order by name"
            val statement = statementBuilder.build(template, mapOf("name" to Value(null, typeOf<String>())), extensions)
            assertEquals("select name, age from person   order by name", statement.toSql())
        }

        @Test
        fun `Remove the 'where' and 'order by' clauses automatically`() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ order by /*%if false*/name/*%end*/"
            val statement = statementBuilder.build(template, mapOf("name" to Value(null, typeOf<String>())), extensions)
            assertEquals("select name, age from person", statement.toSql())
        }
    }

    @Nested
    inner class ForBlockTest {
        @Test
        fun test() {
            val template =
                "select name, age from person where /*%for i in list*/age = /*i*/0 /*%if i_has_next *//*# \"or\" */ /*%end*//*%end*/"
            val statement =
                statementBuilder.build(template, mapOf("list" to Value(listOf(1, 2, 3), typeOf<List<Int>>())), extensions)
            assertEquals("select name, age from person where age = ? or age = ? or age = ? ", statement.toSql())
            assertEquals(
                listOf(
                    Value(1, typeOf<Int>()),
                    Value(2, typeOf<Int>()),
                    Value(3, typeOf<Int>()),
                ),
                statement.args,
            )
        }

        @Test
        fun nestedProperty() {
            val template =
                """
                /*%for item in items*/
                    SELECT
                        /*item.a.b.c*/0 AS id,
                        /*item.description*/'' AS description
                    FROM DUAL
                    /*%if item_has_next*/
                        /*# "UNION"*/
                    /*%end */
                /*%end*/
                """.trimIndent()
            val items = listOf(
                Item(Product1(Product2(1)), "Item1"),
                Item(Product1(Product2(2)), "Item2"),
                Item(Product1(Product2(3)), "Item3"),
            )
            val valueMap = mapOf("items" to Value(items, typeOf<List<Item>>()))
            val statement = statementBuilder.build(template, valueMap, extensions)
            assertEquals(6, statement.args.size)
            assertEquals(1, statement.args[0].any)
            assertEquals("Item1", statement.args[1].any)
            assertEquals(2, statement.args[2].any)
            assertEquals("Item2", statement.args[3].any)
            assertEquals(3, statement.args[4].any)
            assertEquals("Item3", statement.args[5].any)
        }

        @Test
        fun comma() {
            val template =
                "select name, age from person order by /*%for i in list*//*# i *//*# i_next_comma */ /*%end*/"
            val statement =
                statementBuilder.build(template, mapOf("list" to Value(listOf("a", "b", "c"), typeOf<List<Int>>())), extensions)
            assertEquals("select name, age from person order by a, b, c ", statement.toSql())
            assertEquals(
                emptyList(),
                statement.args,
            )
        }

        @Test
        fun and() {
            val template =
                "select name, age from person where /*%for i in list*/age = /*i*/0 /*# i_next_and */ /*%end*/"
            val statement =
                statementBuilder.build(template, mapOf("list" to Value(listOf(1, 2, 3), typeOf<List<Int>>())), extensions)
            assertEquals("select name, age from person where age = ? and age = ? and age = ?  ", statement.toSql())
            assertEquals(
                listOf(
                    Value(1, typeOf<Int>()),
                    Value(2, typeOf<Int>()),
                    Value(3, typeOf<Int>()),
                ),
                statement.args,
            )
        }

        @Test
        fun or() {
            val template =
                "select name, age from person where /*%for i in list*/age = /*i*/0 /*# i_next_or */ /*%end*/"
            val statement =
                statementBuilder.build(template, mapOf("list" to Value(listOf(1, 2, 3), typeOf<List<Int>>())), extensions)
            assertEquals("select name, age from person where age = ? or age = ? or age = ?  ", statement.toSql())
            assertEquals(
                listOf(
                    Value(1, typeOf<Int>()),
                    Value(2, typeOf<Int>()),
                    Value(3, typeOf<Int>()),
                ),
                statement.args,
            )
        }
    }

    @Nested
    inner class WithBlockTest {
        @Test
        fun test() {
            val template =
                "select name, age from person where /*%with item*/name = /*description*/'' and age = /*a.b.c*/0 and weight = /*weight*/0/*%end*/ and location = /*description*/''"
            val statement =
                statementBuilder.build(
                    template,
                    mapOf(
                        "item" to Value(Item(Product1(Product2(1)), "Item1"), typeOf<Item>()),
                        "description" to Value("Hello", typeOf<String>()),
                        "weight" to Value(58, typeOf<Int>()),
                    ),
                    extensions,
                )
            assertEquals("select name, age from person where name = ? and age = ? and weight = ? and location = ?", statement.toSql())
            assertEquals(
                listOf(
                    Value("Item1", typeOf<String>()),
                    Value(1, typeOf<Int>()),
                    Value(58, typeOf<Int>()),
                    Value("Hello", typeOf<String>()),
                ),
                statement.args,
            )
        }

        @Test
        fun cast() {
            val template =
                "select a from b where /*%with shape as @org.komapper.template.Circle@ */c = /*radius*/1.0/*%end*/"
            val statement = statementBuilder.build(
                template,
                mapOf(
                    "shape" to Value(Circle(1.0), typeOf<Shape>()),
                ),
                extensions,
            )
            assertEquals("select a from b where c = ?", statement.toSql())
            assertEquals(
                listOf(
                    Value(1.0, typeOf<Double>()),
                ),
                statement.args,
            )
        }

        @Test
        fun test_null() {
            val template =
                "select name, age from person where /*%with item*/name = /*description*/'' and age = /*a.b.c*/0/*%end*/"
            val e = assertFailsWith<SqlException> {
                statementBuilder.build(
                    template,
                    mapOf(
                        "item" to Value(null, typeOf<Item>()),
                    ),
                    extensions,
                )
            }
            println(e)
        }
    }

    @Nested
    inner class ParenTest {
        @Test
        fun subQuery() {
            val template = "select name, age from (select * from person)"
            val statement = statementBuilder.build(template, emptyMap(), extensions)
            assertEquals("select name, age from (select * from person)", statement.toSql())
        }

        @Test
        fun emptyParentheses() {
            val template = "select name, age from my_function()"
            val statement = statementBuilder.build(template, emptyMap(), extensions)
            assertEquals(template, statement.toSql())
        }
    }

    @Nested
    inner class SetTest {
        @Test
        fun test() {
            val template = "select name from a union select name from b"
            val statement = statementBuilder.build(template, emptyMap(), extensions)
            assertEquals(template, statement.toSql())
        }

        @Test
        fun `Remove the 'union' keyword automatically`() {
            val template = "select name from a union /*%if false*/select name from b/*%end*/"
            val statement = statementBuilder.build(template, emptyMap(), extensions)
            assertEquals("select name from a ", statement.toSql())
        }

        @Test
        fun `Handle multiple 'union' keywords`() {
            val template =
                """
                select name from a
                union
                select name from b
                union
                select name from c
                """.trimIndent()
            val statement = statementBuilder.build(template, emptyMap(), extensions)
            assertEquals(template, statement.toSql())
        }

        @Test
        fun `Handle multiple 'union' keywords and remove a 'union' keyword automatically`() {
            val template =
                """
                select name from a
                union
                /*%if false*/
                select name from b
                /*%end*/
                union
                select name from c
                """.trimIndent()
            val statement = statementBuilder.build(template, emptyMap(), extensions)
            assertEquals(
                """
                select name from a
                union
                select name from c
                """.trimIndent(),
                statement.toSql(),
            )
        }
    }
}

data class Item(
    val a: Product1,
    val description: String,
)

data class Product1(
    val b: Product2,
)

data class Product2(
    val c: Int,
)

sealed interface Color {
    object Red : Color
    object Blue : Color
}

sealed interface Shape
data class Rectangle(val width: Double, val height: Double) : Shape
data class Circle(val radius: Double) : Shape
