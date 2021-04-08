package org.komapper.template.sql

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.template.sql.SqlTokenType.AND
import org.komapper.template.sql.SqlTokenType.BIND_VALUE_DIRECTIVE
import org.komapper.template.sql.SqlTokenType.DELIMITER
import org.komapper.template.sql.SqlTokenType.END_DIRECTIVE
import org.komapper.template.sql.SqlTokenType.EOF
import org.komapper.template.sql.SqlTokenType.EOL
import org.komapper.template.sql.SqlTokenType.EXCEPT
import org.komapper.template.sql.SqlTokenType.FOR_DIRECTIVE
import org.komapper.template.sql.SqlTokenType.FOR_UPDATE
import org.komapper.template.sql.SqlTokenType.FROM
import org.komapper.template.sql.SqlTokenType.GROUP_BY
import org.komapper.template.sql.SqlTokenType.HAVING
import org.komapper.template.sql.SqlTokenType.IF_DIRECTIVE
import org.komapper.template.sql.SqlTokenType.INTERSECT
import org.komapper.template.sql.SqlTokenType.LITERAL_VALUE_DIRECTIVE
import org.komapper.template.sql.SqlTokenType.MINUS
import org.komapper.template.sql.SqlTokenType.MULTI_LINE_COMMENT
import org.komapper.template.sql.SqlTokenType.OPEN_PAREN
import org.komapper.template.sql.SqlTokenType.OPTION
import org.komapper.template.sql.SqlTokenType.OR
import org.komapper.template.sql.SqlTokenType.ORDER_BY
import org.komapper.template.sql.SqlTokenType.QUOTE
import org.komapper.template.sql.SqlTokenType.SELECT
import org.komapper.template.sql.SqlTokenType.SINGLE_LINE_COMMENT
import org.komapper.template.sql.SqlTokenType.SPACE
import org.komapper.template.sql.SqlTokenType.UNION
import org.komapper.template.sql.SqlTokenType.WHERE
import org.komapper.template.sql.SqlTokenType.WORD

class SqlTokenizerTest {

    private lateinit var lineSeparator: String

    @BeforeEach
    fun setUp() {
        lineSeparator = System.getProperty("line.separator")
        System.setProperty("line.separator", "\r\n")
    }

    @AfterEach
    fun tearDown() {
        System.setProperty("line.separator", lineSeparator)
    }

    @Test
    fun testEof() {
        val tokenizer = SqlTokenizer("where")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testDelimiter() {
        val tokenizer = SqlTokenizer("where;")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(DELIMITER, tokenizer.next())
        assertEquals(";", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testLineComment() {
        val tokenizer = SqlTokenizer("where--aaa\r\nbbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SINGLE_LINE_COMMENT, tokenizer.next())
        assertEquals("--aaa", tokenizer.token)
        assertEquals(EOL, tokenizer.next())
        assertEquals("\r\n", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBlockComment() {
        val tokenizer = SqlTokenizer("where /*+aaa*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(MULTI_LINE_COMMENT, tokenizer.next())
        assertEquals("/*+aaa*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBlockComment_empty() {
        val tokenizer = SqlTokenizer("where /**/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(MULTI_LINE_COMMENT, tokenizer.next())
        assertEquals("/**/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testQuote() {
        val tokenizer = SqlTokenizer("where 'aaa'")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(QUOTE, tokenizer.next())
        assertEquals("'aaa'", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testQuote_escaped() {
        val tokenizer = SqlTokenizer("where 'aaa'''")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(QUOTE, tokenizer.next())
        assertEquals("'aaa'''", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testQuote_notClosed() {
        val tokenizer = SqlTokenizer("where 'aaa")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        val exception = assertThrows<SqlException> {
            tokenizer.next()
        }
        println(exception)
    }

    @Test
    fun testQuote_escaped_notClosed() {
        val tokenizer = SqlTokenizer("where 'aaa''bbb''")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        val exception = assertThrows<SqlException> {
            tokenizer.next()
        }
        println(exception)
    }

    @Test
    fun testUnion() {
        val tokenizer = SqlTokenizer("union")
        assertEquals(UNION, tokenizer.next())
        assertEquals("union", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testExcept() {
        val tokenizer = SqlTokenizer("except")
        assertEquals(EXCEPT, tokenizer.next())
        assertEquals("except", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testMinus() {
        val tokenizer = SqlTokenizer("minus")
        assertEquals(MINUS, tokenizer.next())
        assertEquals("minus", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testIntersect() {
        val tokenizer = SqlTokenizer("intersect")
        assertEquals(INTERSECT, tokenizer.next())
        assertEquals("intersect", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testSelect() {
        val tokenizer = SqlTokenizer("select")
        assertEquals(SELECT, tokenizer.next())
        assertEquals("select", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testFrom() {
        val tokenizer = SqlTokenizer("from")
        assertEquals(FROM, tokenizer.next())
        assertEquals("from", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testWhere() {
        val tokenizer = SqlTokenizer("where")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testGroupBy() {
        val tokenizer = SqlTokenizer("group by")
        assertEquals(GROUP_BY, tokenizer.next())
        assertEquals("group by", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testHaving() {
        val tokenizer = SqlTokenizer("having")
        assertEquals(HAVING, tokenizer.next())
        assertEquals("having", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testOrderBy() {
        val tokenizer = SqlTokenizer("order by")
        assertEquals(ORDER_BY, tokenizer.next())
        assertEquals("order by", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testForUpdateBy() {
        val tokenizer = SqlTokenizer("for update")
        assertEquals(FOR_UPDATE, tokenizer.next())
        assertEquals("for update", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testOption() {
        val tokenizer = SqlTokenizer("option (")
        assertEquals(OPTION, tokenizer.next())
        assertEquals("option", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(OPEN_PAREN, tokenizer.next())
        assertEquals("(", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testAnd() {
        val tokenizer = SqlTokenizer("and")
        assertEquals(AND, tokenizer.next())
        assertEquals("and", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testOr() {
        val tokenizer = SqlTokenizer("or")
        assertEquals(OR, tokenizer.next())
        assertEquals("or", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBindBlockComment() {
        val tokenizer = SqlTokenizer("where /*aaa*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(BIND_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*aaa*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBindBlockComment_followingQuote() {
        val tokenizer = SqlTokenizer("where /*aaa*/'2001-01-01 12:34:56'")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(BIND_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*aaa*/", tokenizer.token)
        assertEquals(QUOTE, tokenizer.next())
        assertEquals("'2001-01-01 12:34:56'", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBindBlockComment_followingWordAndQuote() {
        val tokenizer = SqlTokenizer("where /*aaa*/timestamp'2001-01-01 12:34:56' and")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(BIND_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*aaa*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("timestamp'2001-01-01 12:34:56'", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(AND, tokenizer.next())
        assertEquals("and", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBindBlockComment_spaceIncluded() {
        val tokenizer = SqlTokenizer("where /* aaa */bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(BIND_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/* aaa */", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBindBlockComment_startWithStringLiteral() {
        val tokenizer = SqlTokenizer("where /*\"aaa\"*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(BIND_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*\"aaa\"*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testBindBlockComment_startWithCharLiteral() {
        val tokenizer = SqlTokenizer("where /*'a'*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(BIND_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*'a'*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testLiteralBlockComment() {
        val tokenizer = SqlTokenizer("where /*^aaa*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(LITERAL_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*^aaa*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testLiteralBlockComment_followingQuote() {
        val tokenizer = SqlTokenizer("where /*^aaa*/'2001-01-01 12:34:56'")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(LITERAL_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*^aaa*/", tokenizer.token)
        assertEquals(QUOTE, tokenizer.next())
        assertEquals("'2001-01-01 12:34:56'", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testLiteralBlockComment_spaceIncluded() {
        val tokenizer = SqlTokenizer("where /*^ aaa */bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(LITERAL_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/*^ aaa */", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testIfBlockComment() {
        val tokenizer = SqlTokenizer("where /*%if true*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(IF_DIRECTIVE, tokenizer.next())
        assertEquals("/*%if true*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testForBlockComment() {
        val tokenizer = SqlTokenizer("where /*%for element : list*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(FOR_DIRECTIVE, tokenizer.next())
        assertEquals("/*%for element : list*/", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testEndBlockComment() {
        val tokenizer = SqlTokenizer("where bbb/*%end*/")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(END_DIRECTIVE, tokenizer.next())
        assertEquals("/*%end*/", tokenizer.token)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testLineNumber() {
        val tokenizer = SqlTokenizer("aaa\nbbb\nccc\n/* \nddd\n */")
        assertEquals(1, tokenizer.location.lineNumber)
        assertEquals(WORD, tokenizer.next())
        assertEquals("aaa", tokenizer.token)
        assertEquals(1, tokenizer.location.lineNumber)
        assertEquals(EOL, tokenizer.next())
        assertEquals("\n", tokenizer.token)
        assertEquals(2, tokenizer.location.lineNumber)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(2, tokenizer.location.lineNumber)
        assertEquals(EOL, tokenizer.next())
        assertEquals("\n", tokenizer.token)
        assertEquals(3, tokenizer.location.lineNumber)
        assertEquals(WORD, tokenizer.next())
        assertEquals("ccc", tokenizer.token)
        assertEquals(3, tokenizer.location.lineNumber)
        assertEquals(EOL, tokenizer.next())
        assertEquals("\n", tokenizer.token)
        assertEquals(4, tokenizer.location.lineNumber)
        assertEquals(BIND_VALUE_DIRECTIVE, tokenizer.next())
        assertEquals("/* \nddd\n */", tokenizer.token)
        assertEquals(6, tokenizer.location.lineNumber)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testColumnNumber() {
        val tokenizer = SqlTokenizer("aaa bbb\nc\nd eee\n")
        assertEquals(0, tokenizer.location.position)
        assertEquals(WORD, tokenizer.next())
        assertEquals("aaa", tokenizer.token)
        assertEquals(3, tokenizer.location.position)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(4, tokenizer.location.position)
        assertEquals(WORD, tokenizer.next())
        assertEquals("bbb", tokenizer.token)
        assertEquals(7, tokenizer.location.position)
        assertEquals(EOL, tokenizer.next())
        assertEquals("\n", tokenizer.token)
        assertEquals(8, tokenizer.location.position)
        assertEquals(WORD, tokenizer.next())
        assertEquals("c", tokenizer.token)
        assertEquals(1, tokenizer.location.position)
        assertEquals(EOL, tokenizer.next())
        assertEquals("\n", tokenizer.token)
        assertEquals(2, tokenizer.location.position)
        assertEquals(WORD, tokenizer.next())
        assertEquals("d", tokenizer.token)
        assertEquals(1, tokenizer.location.position)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        assertEquals(2, tokenizer.location.position)
        assertEquals(WORD, tokenizer.next())
        assertEquals("eee", tokenizer.token)
        assertEquals(5, tokenizer.location.position)
        assertEquals(EOL, tokenizer.next())
        assertEquals("\n", tokenizer.token)
        assertEquals(6, tokenizer.location.position)
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }

    @Test
    fun testIllegalDirective() {
        val tokenizer = SqlTokenizer("where /*%*/bbb")
        assertEquals(WHERE, tokenizer.next())
        assertEquals("where", tokenizer.token)
        assertEquals(SPACE, tokenizer.next())
        assertEquals(" ", tokenizer.token)
        val exception = assertThrows<SqlException> {
            tokenizer.next()
        }
        println(exception)
    }

    @Test
    fun testEmpty() {
        val tokenizer = SqlTokenizer("")
        assertEquals(EOF, tokenizer.next())
        assertEquals("", tokenizer.token)
    }
}
