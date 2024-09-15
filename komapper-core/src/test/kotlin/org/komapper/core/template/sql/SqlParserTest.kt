package org.komapper.core.template.sql

import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SqlParserTest {

    @Test
    fun empty() {
        val sql = ""
        val node = SqlParser(sql).parse()
        assertEquals(sql, node.toText())
        val statement = node as SqlNode.Statement
        assertTrue(statement.nodeList.isEmpty())
    }

    @Test
    fun simple() {
        val sql = "select * from person"
        val node = SqlParser(sql).parse()
        assertEquals(sql, node.toText())
        val statement = node as SqlNode.Statement
        assertEquals(1, statement.nodeList.size)
        val select = statement.nodeList[0] as SqlNode.Clause.Select
        assertEquals(4, select.nodeList.size)
        select.nodeList[0] as SqlNode.Token.Space
        select.nodeList[1] as SqlNode.Token.Other
        select.nodeList[2] as SqlNode.Token.Space
        val from = select.nodeList[3] as SqlNode.Clause.From
        assertEquals(2, from.nodeList.size)
        select.nodeList[0] as SqlNode.Token.Space
        select.nodeList[1] as SqlNode.Token.Other
    }

    @Test
    fun complex() {
        val sql = "select name, age from person where name = /*name*/'test' and age > 1 order by name, age for update"
        val node = SqlParser(sql).parse()
        assertEquals(sql, node.toText())
    }

    @Nested
    inner class ClauseTest {
        @Test
        fun select() {
            val sql = "select name, age"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun from() {
            val sql = "from person inner join salary"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun where() {
            val sql = "where name = 'aaa'"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun having() {
            val sql = "where name = 'aaa'"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun groupBy() {
            val sql = "group by name, 'aaa'"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun orderBy() {
            val sql = "order by name, 'aaa'"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }
    }

    @Nested
    inner class LogicalOperatorTest {
        @Test
        fun and() {
            val sql = "and age > 1"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun or() {
            val sql = "or age > 1"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }
    }

    @Nested
    inner class BindValueDirectiveTest {
        @Test
        fun bindValue() {
            val sql = "/* age */1"
            val node = SqlParser(sql).parse() as SqlNode.Statement
            assertEquals(sql, node.toText())
            val bindValue = node.nodeList[0] as SqlNode.BindValueDirective
            assertEquals("age", bindValue.expression)
            val word = bindValue.node as SqlNode.Token.Word
            assertEquals("1", word.token)
        }

        @Test
        fun bindValues() {
            val sql = "/* age */(1,2,3)"
            val node = SqlParser(sql).parse() as SqlNode.Statement
            assertEquals(sql, node.toText())
            val bindValue = node.nodeList[0] as SqlNode.BindValueDirective
            assertEquals("age", bindValue.expression)
            bindValue.node as SqlNode.Paren
        }

        @Test
        fun embeddedValue() {
            val sql = "/*# age */"
            val node = SqlParser(sql).parse() as SqlNode.Statement
            assertEquals(sql, node.toText())
            val embeddedValue = node.nodeList[0] as SqlNode.EmbeddedValueDirective
            assertEquals("age", embeddedValue.expression)
        }

        @Test
        fun literalValue() {
            val sql = "/*^ age */1"
            val node = SqlParser(sql).parse() as SqlNode.Statement
            assertEquals(sql, node.toText())
            val literal = node.nodeList[0] as SqlNode.LiteralValueDirective
            assertEquals("age", literal.expression)
            val word = literal.node as SqlNode.Token.Word
            assertEquals("1", word.token)
        }

        @Test
        fun `The expression is not found in the bind value directive`() {
            val sql = "/* */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The expression is not found in the embedded value directive`() {
            val sql = "/*# */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The expression is not found in the literal value directive`() {
            val sql = "/*^ */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The test value must follow the bind value directive`() {
            val sql = "/* aaa */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The test value must follow the literal value directive`() {
            val sql = "/*^ aaa */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }
    }

    @Nested
    inner class CommentTest {
        @Test
        fun multiLineComment() {
            val sql = """
            /**
             * multi-line
             * comment
             */
            """.trimIndent()
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun singleLineComment() {
            val sql = "-- single line comment"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun parserLevelComment() {
            val sql = "select a/*%! comment */ from b"
            val node = SqlParser(sql).parse()
            assertEquals("select a from b", node.toText())
        }

        @Test
        fun `parserLevelComment - space`() {
            val sql = "select a/*% ! comment */ from b"
            val node = SqlParser(sql).parse()
            assertEquals("select a from b", node.toText())
        }
    }

    @Nested
    inner class SetTest {

        @Test
        fun simple() {
            val sql = "select * from person union select * from employee"
            val node = SqlParser(sql).parse() as SqlNode.Set
            assertEquals(sql, node.toText())
            node.left as SqlNode.Statement
            node.right as SqlNode.Statement
        }

        @Test
        fun multiple() {
            val sql = "select * from person union select * from employee union select * from worker"
            val node = SqlParser(sql).parse() as SqlNode.Set
            assertEquals(sql, node.toText())
            val left = node.left as SqlNode.Set
            left.left as SqlNode.Statement
            left.right as SqlNode.Statement
            node.right as SqlNode.Statement
        }
    }

    @Nested
    inner class ParenTest {

        @Test
        fun empty() {
            val sql = "select date()"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun subQuery() {
            val sql = "select * from (select * from person)"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `The close paren is not found`() {
            val sql = "select date("
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }
    }

    @Nested
    inner class IfBlockTest {

        @Test
        fun `if`() {
            val sql = "/*%if a*/ b /*%end*/ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `if - space`() {
            val sql = "/*% if a */ b /*% end */ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun if_elseif() {
            val sql = "/*%if a*/ b /*%elseif c*/ d /*%end*/ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `if_elseif - space`() {
            val sql = "/*% if a */ b /*% elseif c */ d /*% end */ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun if_else() {
            val sql = "/*%if a*/ b /*%else c*/ d /*%end*/ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `if_else - space`() {
            val sql = "/*% if a */ b /*% else c */ d /*% end */ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun if_complex() {
            val sql = "/*%if a*/ b /*%elseif c*/ d /*%elseif e*/ f /*%else*/ g /*%end*/ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun if_for() {
            val sql = "/*%if aaa*/ a /*%for bbb in ccc*/ b /*%end*/ c /*%end*/"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun nestedIf() {
            val sql = "/*%if aaa*/ a /*%if bbb in ccc*/ b /*%end*/ c /*%end*/"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `The corresponding end directive is not found`() {
            val sql = "/*%if aaa*/ a"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `While the elseif directive is used, the corresponding if directive is not found`() {
            val sql = "/*%elseif aaa*/ a"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `While the else directive is used, the corresponding if directive is not found`() {
            val sql = "/*%else*/ a"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The corresponding if directive is not found`() {
            val sql = "/*%end*/ a"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The expression is not found in the if directive`() {
            val sql = "/*%if */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The expression is not found in the elseif directive`() {
            val sql = "/*%elseif */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `illegal elseif`() {
            val sql = "/*%if aaa*/ b /*%else*/ c /*%elseif ddd*/ e /*%end*/"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `illegal else`() {
            val sql = "/*%if aaa*/ b /*%else*/ c /*%else*/ d /*%end*/"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }
    }

    @Nested
    inner class ForBlockTest {

        @Test
        fun simple() {
            val sql = "/*%for a in aaa*/ b /*%end*/ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `simple - space`() {
            val sql = "/*% for a in aaa */ b /*% end */ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun nested() {
            val sql = "/*%for a in aaa*/ b /*%for c in ccc*/ d /*%end*/ e /*%end*/"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `The corresponding end directive is not found`() {
            val sql = "/*%for a in aaa*/ b"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The corresponding for directive is not found`() {
            val sql = "/*%end*/ a"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The statement is not found`() {
            val sql = "/*%for */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The in keyword is not found`() {
            val sql = "/*%for aaa*/"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The identifier is not found`() {
            val sql = "/*%for in aaa*/"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The expression is not found`() {
            val sql = "/*%for a in*/"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }
    }

    @Nested
    inner class WithBlockTest {

        @Test
        fun simple() {
            val sql = "/*%with a */ b /*%end*/ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `simple - space`() {
            val sql = "/*% with a */ b /*% end */ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun nested() {
            val sql = "/*%with a */ b /*%with c */ d /*%end*/ e /*%end*/"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun simple_as() {
            val sql = "/*%with a as x */ b /*%end*/ h"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun nested_as() {
            val sql = "/*%with a as x */ b /*%with c as y */ d /*%end*/ e /*%end*/"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `The corresponding end directive is not found`() {
            val sql = "/*%with a */ b"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The corresponding with directive is not found`() {
            val sql = "/*%end*/ a"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The expression is not found`() {
            val sql = "/*%with */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The left operand is not found`() {
            val sql = "/*%with as x */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }

        @Test
        fun `The right operand is not found`() {
            val sql = "/*%with a as */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }
    }

    @Nested
    inner class PartialTest {
        @Test
        fun simple() {
            val sql = "/*> a */"
            val node = SqlParser(sql).parse()
            assertEquals(sql, node.toText())
        }

        @Test
        fun `The expression is not found in the partial directive`() {
            val sql = "/*>   */"
            val exception = assertFailsWith<SqlException> { SqlParser(sql).parse() }
            println(exception)
        }
    }
}
