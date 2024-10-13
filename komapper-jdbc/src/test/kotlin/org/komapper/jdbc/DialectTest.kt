package org.komapper.jdbc

import org.komapper.core.BuilderDialect
import org.komapper.core.Dialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DialectTest {
    class MyJdbcDialect : Dialect {
        override val driver: String
            get() = throw UnsupportedOperationException()

        override fun getSequenceSql(sequenceName: String): String {
            throw UnsupportedOperationException()
        }

        override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
            throw UnsupportedOperationException()
        }

        override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
            dialect: BuilderDialect,
            context: EntityUpsertContext<ENTITY, ID, META>,
            entities: List<ENTITY>,
        ): EntityUpsertStatementBuilder<ENTITY> {
            throw UnsupportedOperationException()
        }
    }

    private val dialect = MyJdbcDialect()

    @Test
    fun quote() {
        assertEquals("\"aaa\"", dialect.enquote("aaa"))
    }

    @Test
    fun escape() {
        assertEquals("""a\%b\_c\\d|e""", dialect.escape("""a%b_c\d|e"""))
    }

    @Test
    fun escapeWithEscapeString() {
        assertEquals("""a|%b|_c\d||e""", dialect.escape("""a%b_c\d|e""", "|"))
    }
}
