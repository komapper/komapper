package org.komapper.r2dbc

import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.R2dbcException
import org.komapper.core.BuilderDialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class R2dbcDialectsTest {

    @Test
    fun extractR2dbcDriver() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_ssl() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbcs:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_testcontainers() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:tc:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_testcontainers_pool() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:tc:pool:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_pool() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:pool:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_error() {
        assertFailsWith<IllegalStateException> {
            R2dbcDialects.extractR2dbcDriver("jdbc:h2:mem:///testdb")
        }
    }

    private val myDialect = object : R2dbcDialect {
        override val driver: String get() = "myDriver"
        override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean = error("unsupported")
        override fun getSequenceSql(sequenceName: String): String = error("unsupported")
        override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder =
            error("unsupported")

        override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
            dialect: BuilderDialect,
            context: EntityUpsertContext<ENTITY, ID, META>,
            entities: List<ENTITY>,
        ): EntityUpsertStatementBuilder<ENTITY> = error("unsupported")
    }

    private fun getMyDialectOrNull(driver: String): R2dbcDialect? {
        return if (driver == myDialect.driver) myDialect else null
    }

    @Test
    fun getByOptions_found_from_driver() {
        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "myDriver")
            .build()
        val dialect = R2dbcDialects.getByOptions(options, ::getMyDialectOrNull)
        assertSame(myDialect, dialect)
    }

    @Test
    fun getByOptions_not_found_from_driver() {
        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "driver")
            .build()
        assertFailsWith<java.lang.IllegalStateException> {
            R2dbcDialects.getByOptions(options, ::getMyDialectOrNull)
        }
    }

    @Test
    fun getByOptions_found_from_protocol() {
        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "pool")
            .option(ConnectionFactoryOptions.PROTOCOL, "myDriver")
            .build()
        val dialect = R2dbcDialects.getByOptions(options, ::getMyDialectOrNull)
        assertSame(myDialect, dialect)
    }

    @Test
    fun getByOptions_not_found_from_protocol() {
        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "pool")
            .option(ConnectionFactoryOptions.PROTOCOL, "driver")
            .build()
        assertFailsWith<java.lang.IllegalStateException> {
            R2dbcDialects.getByOptions(options, ::getMyDialectOrNull)
        }
    }

    @Test
    fun getByOptions_found_from_colonIncludedProtocol() {
        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "pool")
            .option(ConnectionFactoryOptions.PROTOCOL, "myDriver:myProtocol")
            .build()
        val dialect = R2dbcDialects.getByOptions(options, ::getMyDialectOrNull)
        assertSame(myDialect, dialect)
    }

    @Test
    fun getByOptions_not_found_from_colonIncludedProtocol() {
        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "pool")
            .option(ConnectionFactoryOptions.PROTOCOL, "driver:myProtocol")
            .build()
        assertFailsWith<java.lang.IllegalStateException> {
            R2dbcDialects.getByOptions(options, ::getMyDialectOrNull)
        }
    }
}
