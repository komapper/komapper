package org.komapper.spring.boot.autoconfigure.jdbc

import org.assertj.core.api.Assertions.assertThat
import org.komapper.core.ClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.Statement
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.Value
import org.komapper.dialect.h2.jdbc.H2JdbcDialect
import org.komapper.jdbc.AbstractJdbcDataType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcStringType
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KomapperJdbcAutoConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                DataSourceAutoConfiguration::class.java,
                DataSourceTransactionManagerAutoConfiguration::class.java,
                KomapperJdbcAutoConfiguration::class.java,
            )
        )
        .withPropertyValues(
            "spring.datasource.url=jdbc:h2:mem:example",
        )

    @Test
    fun defaultConfiguration() {
        contextRunner
            .run { context ->
                assertThat(context)
                    .hasNotFailed()
                    .doesNotHaveBean(JdbcDataTypeProvider::class.java)

                val database = context.getBean(JdbcDatabase::class.java)
                assertNotNull(database)
                assertTrue(database.config.dialect is H2JdbcDialect)
                assertNotNull(database.config.templateStatementBuilder)
            }
    }

    @Test
    fun customConfiguration() {
        contextRunner
            .withUserConfiguration(CustomConfigure::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()

                val database = context.getBean(JdbcDatabase::class.java)
                assertNotNull(database)
                val dataType = database.config.dataOperator.getDataType<String>(typeOf<String>())
                assertEquals("abc", dataType.name)
                val clock = database.config.clockProvider.now()
                val timestamp = LocalDateTime.now(clock)
                assertEquals(LocalDateTime.of(2021, 4, 25, 16, 17, 18), timestamp)
                val jdbcOption = database.config.executionOptions
                assertEquals(1234, jdbcOption.queryTimeoutSeconds)
            }
    }

    @Test
    fun templateStatementBuilderConfiguration() {
        contextRunner
            .withUserConfiguration(TemplateStatementBuilderConfigure::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()

                val database = context.getBean(JdbcDatabase::class.java)
                assertNotNull(database)
                val builder = database.config.templateStatementBuilder
                assertTrue(builder is MyStatementBuilder)
            }
    }

    @Test
    fun dataTypeBeanConfiguration() {
        contextRunner
            .withUserConfiguration(JdbcDataTypeConfigure::class.java)
            .run { context ->
                assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(JdbcDataType::class.java)
                    .hasSingleBean(JdbcDataTypeProvider::class.java)

                val dataTypeProvider = context.getBean(JdbcDataTypeProvider::class.java)
                val customDataType = context.getBean(JdbcDataType::class.java)
                assertThat(dataTypeProvider.get<LongRange>(typeOf<LongRange>())).isEqualTo(customDataType)
            }
    }

    @Test
    fun disabledConfiguration() {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KomapperJdbcAutoConfiguration::class.java))
            .withPropertyValues("spring.datasource.url=jdbc:h2:mem:example")
            .run { context ->
                assertThat(context)
                    .hasNotFailed()
                    .doesNotHaveBean(JdbcDatabase::class.java)
            }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    @Configuration
    open class CustomConfigure {
        @Bean
        open fun dataTypeProvider(): JdbcDataTypeProvider {
            return object : JdbcDataTypeProvider {
                private val map: Map<KType, JdbcDataType<*>> =
                    listOf(JdbcStringType("abc")).associateBy { it.type }

                override fun <T : Any> get(type: KType): JdbcDataType<T>? {
                    return map[type] as JdbcDataType<T>?
                }
            }
        }

        @Bean
        open fun clockProvider(): ClockProvider {
            return ClockProvider {
                val timestamp = LocalDateTime.of(2021, 4, 25, 16, 17, 18)
                Clock.fixed(timestamp.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
            }
        }

        @Bean
        open fun jdbcOption(): ExecutionOptions {
            return ExecutionOptions(queryTimeoutSeconds = 1234)
        }
    }

    @Suppress("unused")
    @Configuration
    open class TemplateStatementBuilderConfigure {
        @Bean
        open fun templateStatementBuilder(): TemplateStatementBuilder {
            return MyStatementBuilder()
        }
    }

    @Configuration
    open class JdbcDataTypeConfigure {
        @Bean
        open fun customJdbcDataType(): JdbcDataType<LongRange> = CustomJdbcDataType()
    }

    class MyStatementBuilder : TemplateStatementBuilder {
        override fun build(
            template: CharSequence,
            valueMap: Map<String, Value<*>>,
            builtinExtensions: TemplateBuiltinExtensions,
        ): Statement {
            throw UnsupportedOperationException()
        }

        override fun clearCache() = Unit
    }

    private class CustomJdbcDataType : AbstractJdbcDataType<LongRange>(typeOf<LongRange>(), JDBCType.VARCHAR) {
        override val name = "custom"

        override fun doGetValue(rs: ResultSet, index: Int): LongRange? {
            throw IllegalStateException("Shouldn't be called")
        }

        override fun doGetValue(rs: ResultSet, columnLabel: String): LongRange? {
            throw IllegalStateException("Shouldn't be called")
        }

        override fun doSetValue(ps: PreparedStatement, index: Int, value: LongRange) {
            throw IllegalStateException("Shouldn't be called")
        }
    }
}
