package org.komapper.core.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.SQLException
import kotlin.reflect.KClass

internal class AbstractDialectTest {

    class MyDialect : AbstractDialect() {

        public override fun getDataType(type: KClass<*>): DataType<*> {
            return super.getDataType(type)
        }

        override fun getSequenceSql(sequenceName: String): String {
            throw UnsupportedOperationException()
        }

        override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
            throw UnsupportedOperationException()
        }
    }

    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    private val dialect = MyDialect()

    @Test
    fun getDataType() {
        val dataType = dialect.getDataType(Direction::class)
        assertTrue(EnumType::class.isInstance(dataType))
    }

    @Test
    fun quote() {
        assertEquals("\"aaa\"", dialect.quote("aaa"))
        assertEquals("\"aaa\".\"bbb\".\"ccc\"", dialect.quote("aaa.bbb.ccc"))
    }

    @Test
    fun escape() {
        assertEquals("""a\%b\_c\\d""", dialect.escape("""a%b_c\d"""))
    }
}
