package org.komapper.core.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.AbstractDialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import java.sql.SQLException
import kotlin.reflect.KClass

internal class AbstractDialectTest {

    class MyDialect : AbstractDialect() {

        override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
            throw UnsupportedOperationException()
        }

        override fun getSequenceSql(sequenceName: String): String {
            throw UnsupportedOperationException()
        }

        override fun getDataType(type: KClass<*>): Pair<DataType<*>, String> {
            throw UnsupportedOperationException()
        }

        override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
            throw UnsupportedOperationException()
        }

        override fun <ENTITY : Any> getEntityUpsertStatementBuilder(
            context: EntityUpsertContext<ENTITY>,
            entity: ENTITY
        ): EntityUpsertStatementBuilder<ENTITY> {
            throw UnsupportedOperationException()
        }
    }

    private val dialect = MyDialect()

    @Test
    fun quote() {
        assertEquals("\"aaa\"", dialect.enquote("aaa"))
    }

    @Test
    fun escape() {
        assertEquals("""a\%b\_c\\d""", dialect.escape("""a%b_c\d"""))
    }
}
