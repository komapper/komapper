package org.komapper.core.template.sql

import org.komapper.core.template.sql.SqlTokenType.AND
import org.komapper.core.template.sql.SqlTokenType.BIND_VALUE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.CLOSE_PAREN
import org.komapper.core.template.sql.SqlTokenType.DELIMITER
import org.komapper.core.template.sql.SqlTokenType.ELSEIF_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.ELSE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.EMBEDDED_VALUE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.END_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.EOF
import org.komapper.core.template.sql.SqlTokenType.EOL
import org.komapper.core.template.sql.SqlTokenType.EXCEPT
import org.komapper.core.template.sql.SqlTokenType.FOR_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.FOR_UPDATE
import org.komapper.core.template.sql.SqlTokenType.FROM
import org.komapper.core.template.sql.SqlTokenType.GROUP_BY
import org.komapper.core.template.sql.SqlTokenType.HAVING
import org.komapper.core.template.sql.SqlTokenType.IF_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.INTERSECT
import org.komapper.core.template.sql.SqlTokenType.LITERAL_VALUE_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.MINUS
import org.komapper.core.template.sql.SqlTokenType.MULTI_LINE_COMMENT
import org.komapper.core.template.sql.SqlTokenType.OPEN_PAREN
import org.komapper.core.template.sql.SqlTokenType.OPTION
import org.komapper.core.template.sql.SqlTokenType.OR
import org.komapper.core.template.sql.SqlTokenType.ORDER_BY
import org.komapper.core.template.sql.SqlTokenType.OTHER
import org.komapper.core.template.sql.SqlTokenType.PARSER_LEVEL_COMMENT_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.PARTIAL_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.QUOTE
import org.komapper.core.template.sql.SqlTokenType.SELECT
import org.komapper.core.template.sql.SqlTokenType.SINGLE_LINE_COMMENT
import org.komapper.core.template.sql.SqlTokenType.SPACE
import org.komapper.core.template.sql.SqlTokenType.UNION
import org.komapper.core.template.sql.SqlTokenType.WHERE
import org.komapper.core.template.sql.SqlTokenType.WITH_DIRECTIVE
import org.komapper.core.template.sql.SqlTokenType.WORD
import java.nio.CharBuffer

internal class SqlTokenizer(private val sql: String) {

    companion object {
        const val LOOKAHEAD_SIZE: Int = 10
    }

    private val lookahead = CharArray(LOOKAHEAD_SIZE)
    private val buf: CharBuffer = CharBuffer.wrap(sql)
    private val tokenBuf: CharBuffer = buf.asReadOnlyBuffer()
    private var lineNumber: Int = 1
    private var lineStartPosition: Int = 0
    private var type: SqlTokenType = EOF
    var token: String = ""
        private set
    private var startColumnIndex: Int = 0

    val location
        get() = SqlLocation(sql, lineNumber, startColumnIndex, buf.position() - lineStartPosition)

    fun next(): SqlTokenType {
        if (type == EOL) {
            lineStartPosition = buf.position()
        }
        startColumnIndex = buf.position() - lineStartPosition
        read()
        tokenBuf.limit(buf.position())
        token = tokenBuf.toString()
        tokenBuf.position(buf.position())
        return type
    }

    private fun read() {
        val length = buf.remaining().coerceAtMost(LOOKAHEAD_SIZE)
        buf.get(lookahead, 0, length)
        when (length) {
            10 -> readTenChars(lookahead)
            9 -> readNineChars(lookahead)
            8 -> readEightChars(lookahead)
            7 -> readSevenChars(lookahead)
            6 -> readSixChars(lookahead)
            5 -> readFiveChars(lookahead)
            4 -> readFourChars(lookahead)
            3 -> readThreeChars(lookahead)
            2 -> readTwoChars(lookahead)
            1 -> readOneChar(lookahead[0])
            0 -> type = EOF
            else -> error(length)
        }
    }

    private fun readTenChars(c: CharArray) {
        if ((c[0] == 'f' || c[0] == 'F') &&
            (c[1] == 'o' || c[1] == 'O') &&
            (c[2] == 'r' || c[2] == 'R') &&
            c[3].isSpace() &&
            (c[4] == 'u' || c[4] == 'U') &&
            (c[5] == 'p' || c[5] == 'P') &&
            (c[6] == 'd' || c[6] == 'D') &&
            (c[7] == 'a' || c[7] == 'A') &&
            (c[8] == 't' || c[8] == 'T') &&
            (c[9] == 'e' || c[9] == 'E')
        ) {
            type = FOR_UPDATE
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        readNineChars(c)
    }

    private fun readNineChars(c: CharArray) {
        if ((c[0] == 'i' || c[0] == 'I') &&
            (c[1] == 'n' || c[1] == 'N') &&
            (c[2] == 't' || c[2] == 'T') &&
            (c[3] == 'e' || c[3] == 'E') &&
            (c[4] == 'r' || c[4] == 'R') &&
            (c[5] == 's' || c[5] == 'S') &&
            (c[6] == 'e' || c[6] == 'E') &&
            (c[7] == 'c' || c[7] == 'C') &&
            (c[8] == 't' || c[8] == 'T')
        ) {
            type = INTERSECT
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        readEightChars(c)
    }

    private fun readEightChars(c: CharArray) {
        if ((c[0] == 'g' || c[0] == 'G') &&
            (c[1] == 'r' || c[1] == 'R') &&
            (c[2] == 'o' || c[2] == 'O') &&
            (c[3] == 'u' || c[3] == 'U') &&
            (c[4] == 'p' || c[4] == 'P') &&
            c[5].isSpace() &&
            (c[6] == 'b' || c[6] == 'B') &&
            (c[7] == 'y' || c[7] == 'Y')
        ) {
            type = GROUP_BY
            if (isWordTerminated()) {
                return
            }
        } else if ((c[0] == 'o' || c[0] == 'O') &&
            (c[1] == 'r' || c[1] == 'R') &&
            (c[2] == 'd' || c[2] == 'D') &&
            (c[3] == 'e' || c[3] == 'E') &&
            (c[4] == 'r' || c[4] == 'R') &&
            c[5].isSpace() &&
            (c[6] == 'b' || c[6] == 'B') &&
            (c[7] == 'y' || c[7] == 'Y')
        ) {
            type = ORDER_BY
            if (isWordTerminated()) {
                return
            }
        } else if ((c[0] == 'o' || c[0] == 'O') &&
            (c[1] == 'p' || c[1] == 'P') &&
            (c[2] == 't' || c[2] == 'T') &&
            (c[3] == 'i' || c[3] == 'I') &&
            (c[4] == 'o' || c[4] == 'O') &&
            (c[5] == 'n' || c[5] == 'N') &&
            c[6].isSpace() &&
            c[7] == '('
        ) {
            type = OPTION
            buf.position(buf.position() - 2)
            return
        }
        buf.position(buf.position() - 1)
        readSevenChars(c)
    }

    private fun readSevenChars(c: CharArray) {
        buf.position(buf.position() - 1)
        readSixChars(c)
    }

    private fun readSixChars(c: CharArray) {
        if ((c[0] == 's' || c[0] == 'S') &&
            (c[1] == 'e' || c[1] == 'E') &&
            (c[2] == 'l' || c[2] == 'L') &&
            (c[3] == 'e' || c[3] == 'E') &&
            (c[4] == 'c' || c[4] == 'C') &&
            (c[5] == 't' || c[5] == 'T')
        ) {
            type = SELECT
            if (isWordTerminated()) {
                return
            }
        } else if ((c[0] == 'h' || c[0] == 'H') &&
            (c[1] == 'a' || c[1] == 'A') &&
            (c[2] == 'v' || c[2] == 'V') &&
            (c[3] == 'i' || c[3] == 'I') &&
            (c[4] == 'n' || c[4] == 'N') &&
            (c[5] == 'g' || c[5] == 'G')
        ) {
            type = HAVING
            if (isWordTerminated()) {
                return
            }
        } else if ((c[0] == 'e' || c[0] == 'E') &&
            (c[1] == 'x' || c[1] == 'X') &&
            (c[2] == 'c' || c[2] == 'C') &&
            (c[3] == 'e' || c[3] == 'E') &&
            (c[4] == 'p' || c[4] == 'P') &&
            (c[5] == 't' || c[5] == 'T')
        ) {
            type = EXCEPT
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        readFiveChars(c)
    }

    private fun readFiveChars(c: CharArray) {
        if ((c[0] == 'w' || c[0] == 'W') &&
            (c[1] == 'h' || c[1] == 'H') &&
            (c[2] == 'e' || c[2] == 'E') &&
            (c[3] == 'r' || c[3] == 'R') &&
            (c[4] == 'e' || c[4] == 'E')
        ) {
            type = WHERE
            if (isWordTerminated()) {
                return
            }
        } else if ((c[0] == 'u' || c[0] == 'U') &&
            (c[1] == 'n' || c[1] == 'N') &&
            (c[2] == 'i' || c[2] == 'I') &&
            (c[3] == 'o' || c[3] == 'O') &&
            (c[4] == 'n' || c[4] == 'N')
        ) {
            type = UNION
            if (isWordTerminated()) {
                return
            }
        } else if ((c[0] == 'm' || c[0] == 'M') &&
            (c[1] == 'i' || c[1] == 'I') &&
            (c[2] == 'n' || c[2] == 'N') &&
            (c[3] == 'u' || c[3] == 'U') &&
            (c[4] == 's' || c[4] == 'S')
        ) {
            type = MINUS
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        readFourChars(c)
    }

    private fun readFourChars(c: CharArray) {
        if ((c[0] == 'f' || c[0] == 'F') &&
            (c[1] == 'r' || c[1] == 'R') &&
            (c[2] == 'o' || c[2] == 'O') &&
            (c[3] == 'm' || c[3] == 'M')
        ) {
            type = FROM
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        readThreeChars(c)
    }

    private fun readThreeChars(c: CharArray) {
        if ((c[0] == 'a' || c[0] == 'A') && (c[1] == 'n' || c[1] == 'N') && (c[2] == 'd' || c[2] == 'D')) {
            type = AND
            if (isWordTerminated()) {
                return
            }
        }
        buf.position(buf.position() - 1)
        readTwoChars(c)
    }

    private fun readTwoChars(c: CharArray) {
        if ((c[0] == 'o' || c[0] == 'O') && (c[1] == 'r' || c[1] == 'R')) {
            type = OR
            if (isWordTerminated()) {
                return
            }
        } else if (c[0] == '/' && c[1] == '*') {
            type = MULTI_LINE_COMMENT
            if (buf.hasRemaining()) {
                val c2 = buf.get()
                if (c2.isExpressionIdentifierStart()) {
                    type = BIND_VALUE_DIRECTIVE
                } else if (c2 == '^') {
                    type = LITERAL_VALUE_DIRECTIVE
                } else if (c2 == '#') {
                    type = EMBEDDED_VALUE_DIRECTIVE
                } else if (c2 == '>') {
                    type = PARTIAL_DIRECTIVE
                } else if (c2 == '%') {
                    while (buf.hasRemaining()) {
                        buf.mark()
                        val c3 = buf.get()
                        if (!c3.isWhitespace()) {
                            buf.reset()
                            break
                        }
                    }
                    if (buf.hasRemaining()) {
                        val c3 = buf.get()
                        if (c3 == '!') {
                            type = PARSER_LEVEL_COMMENT_DIRECTIVE
                        } else if (buf.hasRemaining()) {
                            val c4 = buf.get()
                            if (c3 == 'i' && c4 == 'f') {
                                if (isDirectiveTerminated()) {
                                    type = IF_DIRECTIVE
                                }
                            } else if (buf.hasRemaining()) {
                                val c5 = buf.get()
                                if (c3 == 'f' && c4 == 'o' && c5 == 'r') {
                                    if (isDirectiveTerminated()) {
                                        type = FOR_DIRECTIVE
                                    }
                                } else if (c3 == 'e' && c4 == 'n' && c5 == 'd') {
                                    if (isDirectiveTerminated()) {
                                        type = END_DIRECTIVE
                                    }
                                } else if (buf.hasRemaining()) {
                                    val c6 = buf.get()
                                    if (c3 == 'e' && c4 == 'l' && c5 == 's' && c6 == 'e') {
                                        if (isDirectiveTerminated()) {
                                            type = ELSE_DIRECTIVE
                                        } else {
                                            if (buf.hasRemaining()) {
                                                val c7 = buf.get()
                                                if (buf.hasRemaining()) {
                                                    val c8 = buf.get()
                                                    if (c7 == 'i' && c8 == 'f') {
                                                        if (isDirectiveTerminated()) {
                                                            type = ELSEIF_DIRECTIVE
                                                        }
                                                    } else {
                                                        buf.position(buf.position() - 6)
                                                    }
                                                } else {
                                                    buf.position(buf.position() - 5)
                                                }
                                            }
                                        }
                                    } else if (c3 == 'w' && c4 == 'i' && c5 == 't' && c6 == 'h') {
                                        if (isDirectiveTerminated()) {
                                            type = WITH_DIRECTIVE
                                        }
                                    } else {
                                        buf.position(buf.position() - 4)
                                    }
                                } else {
                                    buf.position(buf.position() - 3)
                                }
                            } else {
                                buf.position(buf.position() - 2)
                            }
                        } else {
                            buf.position(buf.position() - 1)
                        }
                    }
                    if (type !== PARSER_LEVEL_COMMENT_DIRECTIVE &&
                        type !== IF_DIRECTIVE &&
                        type !== FOR_DIRECTIVE &&
                        type !== WITH_DIRECTIVE &&
                        type !== END_DIRECTIVE &&
                        type !== ELSE_DIRECTIVE &&
                        type !== ELSEIF_DIRECTIVE &&
                        type !== PARTIAL_DIRECTIVE
                    ) {
                        throw SqlException("Unsupported directive name is found at $location")
                    }
                }
                buf.position(buf.position() - 1)
            }
            while (buf.hasRemaining()) {
                val c2 = buf.get()
                if (buf.hasRemaining()) {
                    buf.mark()
                    val c3 = buf.get()
                    if (c2 == '*' && c3 == '/') {
                        return
                    }
                    if (c2 == '\r' && c3 == '\n' || c2 == '\r' || c2 == '\n') {
                        lineNumber++
                    }
                    buf.reset()
                }
            }
            throw SqlException("The token \"*/\" for the end of the multi-line comment is not found at $location")
        } else if (c[0] == '-' && c[1] == '-') {
            type = SINGLE_LINE_COMMENT
            while (buf.hasRemaining()) {
                buf.mark()
                val c2 = buf.get()
                if (c2 == '\r' || c2 == '\n') {
                    buf.reset()
                    return
                }
            }
            return
        } else if (c[0] == '\r' && c[1] == '\n') {
            type = EOL
            lineNumber++
            return
        }
        buf.position(buf.position() - 1)
        readOneChar(c[0])
    }

    private fun readOneChar(c: Char) {
        if (c.isSpace()) {
            type = SPACE
        } else if (c == '(') {
            type = OPEN_PAREN
        } else if (c == ')') {
            type = CLOSE_PAREN
        } else if (c == ';') {
            type = DELIMITER
        } else if (c == '\'') {
            type = QUOTE
            var closed = false
            while (buf.hasRemaining()) {
                val c1 = buf.get()
                if (c1 == '\'') {
                    if (buf.hasRemaining()) {
                        buf.mark()
                        val c2 = buf.get()
                        if (c2 != '\'') {
                            buf.reset()
                            closed = true
                            break
                        }
                    } else {
                        closed = true
                    }
                }
            }
            if (closed) {
                return
            }
            throw SqlException("The token \"'\" for the end of the string literal is not found at $location")
        } else if (isWordStart(c)) {
            type = WORD
            while (buf.hasRemaining()) {
                buf.mark()
                val c1 = buf.get()
                if (c1 == '\'') {
                    var closed = false
                    while (buf.hasRemaining()) {
                        val c2 = buf.get()
                        if (c2 == '\'') {
                            if (buf.hasRemaining()) {
                                buf.mark()
                                val c3 = buf.get()
                                if (c3 != '\'') {
                                    buf.reset()
                                    closed = true
                                    break
                                }
                            } else {
                                closed = true
                            }
                        }
                    }
                    if (closed) {
                        return
                    }
                    throw SqlException("The token \"'\" for the end of the string literal is not found at $location")
                }
                if (!c1.isWordPart()) {
                    buf.reset()
                    return
                }
            }
        } else if (c == '\r' || c == '\n') {
            type = EOL
            lineNumber++
        } else {
            type = OTHER
        }
    }

    private fun isWordStart(c: Char): Boolean {
        if (c == '+' || c == '-') {
            buf.mark()
            if (buf.hasRemaining()) {
                val c1 = buf.get()
                buf.reset()
                if (c1.isDigit()) {
                    return true
                }
            }
        }
        return c.isWordPart()
    }

    private fun isWordTerminated(): Boolean {
        buf.mark()
        if (buf.hasRemaining()) {
            val c = buf.get()
            buf.reset()
            if (!c.isWordPart()) {
                return true
            }
        } else {
            return true
        }
        return false
    }

    private fun isDirectiveTerminated(): Boolean {
        buf.mark()
        if (buf.hasRemaining()) {
            val c = buf.get()
            buf.reset()
            if (!c.isWordPart()) {
                return true
            }
        } else {
            return true
        }
        return false
    }
}

private fun Char.isWordPart(): Boolean = if (this.isWhitespace()) {
    false
} else {
    when (this) {
        '=', '<', '>', '-', ',', '/', '*', '+', '(', ')', ';' -> false
        else -> true
    }
}

private fun Char.isExpressionIdentifierStart(): Boolean =
    (this.isJavaIdentifierStart() || this.isWhitespace() || this == '"' || this == '\'')

private fun Char.isSpace(): Boolean = when (this) {
    '\u0009', '\u000B', '\u000C', '\u001C', '\u001D', '\u001E', '\u001F', '\u0020' -> true
    else -> false
}
