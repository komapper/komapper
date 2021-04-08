package org.komapper.core.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.config.EmptyDialect
import kotlin.reflect.KClass

internal class AbstractDialectTest {

    class MyDialect : EmptyDialect() {

        public override fun getDataType(type: KClass<*>): DataType<*> {
            return super.getDataType(type)
        }
    }

    private val dialect = MyDialect()

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
