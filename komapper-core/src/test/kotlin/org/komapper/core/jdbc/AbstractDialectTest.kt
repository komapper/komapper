package org.komapper.core.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.AbstractDialect
import org.komapper.core.dsl.builder.EntityMultiUpsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
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

        override fun getDataType(klass: KClass<*>): DataType<*> {
            throw UnsupportedOperationException()
        }

        override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
            throw UnsupportedOperationException()
        }

        override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
            context: EntityUpsertContext<ENTITY, ID, META>,
            entity: ENTITY
        ): EntityUpsertStatementBuilder<ENTITY> {
            throw UnsupportedOperationException()
        }

        override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityMultiUpsertStatementBuilder(
            context: EntityUpsertContext<ENTITY, ID, META>,
            entities: List<ENTITY>
        ): EntityMultiUpsertStatementBuilder<ENTITY> {
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
        assertEquals("""a\%b\_c\\d|e""", dialect.escape("""a%b_c\d|e"""))
    }

    @Test
    fun escapeWithEscapeString() {
        assertEquals("""a|%b|_c\d||e""", dialect.escape("""a%b_c\d|e""", "|"))
    }
}
