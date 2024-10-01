package org.komapper.core.template.expression

import org.komapper.core.template.expression.ExprTokenType.AND
import org.komapper.core.template.expression.ExprTokenType.AS
import org.komapper.core.template.expression.ExprTokenType.BIG_DECIMAL
import org.komapper.core.template.expression.ExprTokenType.CALLABLE_VALUE
import org.komapper.core.template.expression.ExprTokenType.CHAR
import org.komapper.core.template.expression.ExprTokenType.CLASS_REF
import org.komapper.core.template.expression.ExprTokenType.CLOSE_PAREN
import org.komapper.core.template.expression.ExprTokenType.COMMA
import org.komapper.core.template.expression.ExprTokenType.DOUBLE
import org.komapper.core.template.expression.ExprTokenType.EOE
import org.komapper.core.template.expression.ExprTokenType.EOL
import org.komapper.core.template.expression.ExprTokenType.EQ
import org.komapper.core.template.expression.ExprTokenType.FALSE
import org.komapper.core.template.expression.ExprTokenType.FLOAT
import org.komapper.core.template.expression.ExprTokenType.FUNCTION
import org.komapper.core.template.expression.ExprTokenType.GE
import org.komapper.core.template.expression.ExprTokenType.GT
import org.komapper.core.template.expression.ExprTokenType.ILLEGAL_NUMBER
import org.komapper.core.template.expression.ExprTokenType.INT
import org.komapper.core.template.expression.ExprTokenType.IS
import org.komapper.core.template.expression.ExprTokenType.LE
import org.komapper.core.template.expression.ExprTokenType.LONG
import org.komapper.core.template.expression.ExprTokenType.LT
import org.komapper.core.template.expression.ExprTokenType.NE
import org.komapper.core.template.expression.ExprTokenType.NOT
import org.komapper.core.template.expression.ExprTokenType.NULL
import org.komapper.core.template.expression.ExprTokenType.OPEN_PAREN
import org.komapper.core.template.expression.ExprTokenType.OR
import org.komapper.core.template.expression.ExprTokenType.OTHER
import org.komapper.core.template.expression.ExprTokenType.PROPERTY
import org.komapper.core.template.expression.ExprTokenType.SAFE_CALL_FUNCTION
import org.komapper.core.template.expression.ExprTokenType.SAFE_CALL_PROPERTY
import org.komapper.core.template.expression.ExprTokenType.STRING
import org.komapper.core.template.expression.ExprTokenType.TRUE
import org.komapper.core.template.expression.ExprTokenType.VALUE
import org.komapper.core.template.expression.ExprTokenType.WHITESPACE
import java.nio.CharBuffer

class ExprTokenizer(private val expression: String) {

    companion object {
        const val LOOKAHEAD_SIZE: Int = 5
    }

    private val lookahead = CharArray(LOOKAHEAD_SIZE)
    private val buf = CharBuffer.wrap(expression)
    private val tokenBuf = buf.asReadOnlyBuffer()
    private var lineNumber: Int = 1
    private var lineStartPosition: Int = 0
    private var binaryOpAvailable = false
    private var type = EOE
    var token = ""
    private var startColumnIndex = 0
    val location
        get() = ExprLocation(expression, buf.position(), lineNumber, startColumnIndex)

    operator fun next(): ExprTokenType {
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
            5 -> readFiveChars(lookahead)
            4 -> readFourChars(lookahead)
            3 -> readThreeChars(lookahead)
            2 -> readTwoChars(lookahead)
            1 -> readOneChar(lookahead[0])
            0 -> type = EOE
            else -> error(length)
        }
    }

    private fun readFiveChars(c: CharArray) {
        if (c[0] == 'f' && c[1] == 'a' && c[2] == 'l' && c[3] == 's' && c[4] == 'e') {
            if (isWordTerminated()) {
                type = FALSE
                binaryOpAvailable = true
                return
            }
        }
        buf.position(buf.position() - 1)
        readFourChars(c)
    }

    private fun readFourChars(c: CharArray) {
        if (c[0] == 'n' && c[1] == 'u' && c[2] == 'l' && c[3] == 'l') {
            if (isWordTerminated()) {
                type = NULL
                binaryOpAvailable = true
                return
            }
        } else if (c[0] == 't' && c[1] == 'r' && c[2] == 'u' && c[3] == 'e') {
            if (isWordTerminated()) {
                type = TRUE
                binaryOpAvailable = true
                return
            }
        }
        buf.position(buf.position() - 1)
        readThreeChars(c)
    }

    private fun readThreeChars(c: CharArray) {
        buf.position(buf.position() - 1)
        readTwoChars(c)
    }

    private fun readTwoChars(c: CharArray) {
        if (binaryOpAvailable) {
            if (c[0] == '&' && c[1] == '&') {
                type = AND
                binaryOpAvailable = false
                return
            } else if (c[0] == '|' && c[1] == '|') {
                type = OR
                binaryOpAvailable = false
                return
            } else if (c[0] == '=' && c[1] == '=') {
                type = EQ
                binaryOpAvailable = false
                return
            } else if (c[0] == '!' && c[1] == '=') {
                type = NE
                binaryOpAvailable = false
                return
            } else if (c[0] == '>' && c[1] == '=') {
                type = GE
                binaryOpAvailable = false
                return
            } else if (c[0] == '<' && c[1] == '=') {
                type = LE
                binaryOpAvailable = false
                return
            } else if (c[0] == 'i' && c[1] == 's') {
                type = IS
                binaryOpAvailable = false
                return
            } else if (c[0] == 'a' && c[1] == 's') {
                type = AS
                binaryOpAvailable = false
                return
            }
        }
        if (c[0] == '?' && c[1] == '.') {
            type = SAFE_CALL_PROPERTY
            binaryOpAvailable = true
            if (!buf.hasRemaining()) {
                throw ExprException("Either property or function name must follow the safe call operator at $location")
            }
            buf.mark()
            val c1 = buf.get()
            if (c1.isJavaIdentifierStart()) {
                while (buf.hasRemaining()) {
                    buf.mark()
                    val c2 = buf.get()
                    if (!c2.isJavaIdentifierPart()) {
                        if (c2 == '(') {
                            type = SAFE_CALL_FUNCTION
                            binaryOpAvailable = false
                        }
                        buf.reset()
                        return
                    }
                }
            } else {
                throw ExprException("The character \"$c1\" is illegal as an identifier start at $location")
            }
            return
        }
        if (c[0] == '\r' && c[1] == '\n') {
            type = EOL
            lineNumber++
            return
        }
        buf.position(buf.position() - 1)
        readOneChar(c[0])
    }

    private fun readOneChar(c: Char) {
        if (binaryOpAvailable) {
            if (c == '>') {
                type = GT
                binaryOpAvailable = false
                return
            } else if (c == '<') {
                type = LT
                binaryOpAvailable = false
                return
            }
        }
        if (c == '\r' || c == '\n') {
            type = EOL
            lineNumber++
            return
        } else if (c.isWhitespace()) {
            type = WHITESPACE
            return
        } else if (c == ',') {
            type = COMMA
            return
        } else if (c == '(') {
            type = OPEN_PAREN
            return
        } else if (c == ')') {
            type = CLOSE_PAREN
            binaryOpAvailable = true
            return
        } else if (c == '!') {
            type = NOT
            return
        } else if (c == '\'') {
            type = CHAR
            if (buf.hasRemaining()) {
                buf.get()
                if (buf.hasRemaining()) {
                    val c2 = buf.get()
                    if (c2 == '\'') {
                        binaryOpAvailable = true
                        return
                    }
                }
            }
            throw ExprException("The end of single quotation mark is not found at $location")
        } else if (c == '"') {
            type = STRING
            var closed = false
            while (buf.hasRemaining()) {
                val c1 = buf.get()
                if (c1 == '"') {
                    if (buf.hasRemaining()) {
                        buf.mark()
                        val c2 = buf.get()
                        if (c2 != '"') {
                            buf.reset()
                            closed = true
                            break
                        }
                    } else {
                        closed = true
                    }
                }
            }
            if (!closed) {
                throw ExprException("The end of double quotation mark is not found at $location")
            }
            binaryOpAvailable = true
        } else if (c == '@') {
            type = CLASS_REF
            var closed = false
            while (buf.hasRemaining()) {
                val c1 = buf.get()
                if (c1 == '@') {
                    closed = true
                    break
                }
            }
            if (!closed) {
                throw ExprException("The end of at sign is not found at $location")
            }
            binaryOpAvailable = true
        } else if (c == '+' || c == '-') {
            buf.mark()
            if (buf.hasRemaining()) {
                val c1 = buf.get()
                if (c1.isDigit()) {
                    readNumber()
                    return
                }
                buf.reset()
            }
            type = ILLEGAL_NUMBER
        } else if (c.isDigit()) {
            readNumber()
        } else if (c.isJavaIdentifierStart()) {
            type = VALUE
            binaryOpAvailable = true
            while (buf.hasRemaining()) {
                buf.mark()
                val c1 = buf.get()
                if (!c1.isJavaIdentifierPart()) {
                    buf.reset()
                    break
                }
            }
            if (buf.hasRemaining()) {
                buf.mark()
                val c1 = buf.get()
                if (c1 == '(') {
                    type = CALLABLE_VALUE
                    binaryOpAvailable = false
                }
                buf.reset()
            }
        } else if (c == '.') {
            type = PROPERTY
            binaryOpAvailable = true
            if (!buf.hasRemaining()) {
                throw ExprException("Either property or function name must follow the dot at $location")
            }
            buf.mark()
            val c1 = buf.get()
            if (c1.isJavaIdentifierStart()) {
                while (buf.hasRemaining()) {
                    buf.mark()
                    val c2 = buf.get()
                    if (!c2.isJavaIdentifierPart()) {
                        if (c2 == '(') {
                            type = FUNCTION
                            binaryOpAvailable = false
                        }
                        buf.reset()
                        return
                    }
                }
            } else {
                throw ExprException("The character \"$c1\" is illegal as an identifier start at $location")
            }
        } else {
            type = OTHER
        }
    }

    private fun readNumber() {
        type = INT
        var decimal = false
        while (buf.hasRemaining()) {
            buf.mark()
            val c = buf.get()
            if (c.isDigit()) {
                continue
            } else if (c == '.') {
                if (decimal) {
                    type = ILLEGAL_NUMBER
                    return
                }
                decimal = true
                if (buf.hasRemaining()) {
                    val c2 = buf.get()
                    if (!c2.isDigit()) {
                        type = ILLEGAL_NUMBER
                        return
                    }
                } else {
                    type = ILLEGAL_NUMBER
                    return
                }
            } else if (c == 'F') {
                type = FLOAT
                break
            } else if (c == 'D') {
                type = DOUBLE
                break
            } else if (c == 'L') {
                type = LONG
                break
            } else if (c == 'B') {
                type = BIG_DECIMAL
                break
            } else {
                buf.reset()
                break
            }
        }
        if (!isWordTerminated()) {
            type = ILLEGAL_NUMBER
        }
        binaryOpAvailable = true
    }

    private fun isWordTerminated(): Boolean {
        buf.mark()
        if (buf.hasRemaining()) {
            val c = buf.get()
            if (!c.isJavaIdentifierPart()) {
                buf.reset()
                return true
            }
        } else {
            return true
        }
        return false
    }
}
