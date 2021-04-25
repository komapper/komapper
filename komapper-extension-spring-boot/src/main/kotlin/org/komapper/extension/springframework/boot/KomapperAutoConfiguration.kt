package org.komapper.extension.springframework.boot

import org.komapper.core.ClockProvider
import org.komapper.core.Database
import org.komapper.core.DatabaseConfig
import org.komapper.core.DefaultClockProvider
import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.Dialect
import org.komapper.core.data.JdbcOption
import org.komapper.core.jdbc.DataType
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import javax.sql.DataSource

@Suppress("unused")
@Configuration
@ConditionalOnClass(Database::class)
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
open class KomapperAutoConfiguration {

    companion object {
        private const val DATASOURCE_URL_PROPERTY = "spring.datasource.url"
    }

    @Bean
    @ConditionalOnMissingBean
    open fun dialect(environment: Environment, dataTypes: Set<DataType<*>> = emptySet()): Dialect {
        val url: String = environment.getProperty(DATASOURCE_URL_PROPERTY)
            ?: error(
                "$DATASOURCE_URL_PROPERTY is not found. " +
                    "Specify it to the application.properties file or define the dialect bean manually."
            )
        return Dialect.load(url, dataTypes)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun clockProvider(): ClockProvider {
        return DefaultClockProvider()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun jdbcOption(): JdbcOption {
        return JdbcOption()
    }

    @Bean
    @ConditionalOnMissingBean(Database::class)
    open fun database(
        dataSource: DataSource,
        dialect: Dialect,
        clockProvider: ClockProvider,
        jdbcOption: JdbcOption
    ): Database {
        val proxy = if (dataSource is TransactionAwareDataSourceProxy) {
            dataSource
        } else {
            TransactionAwareDataSourceProxy(dataSource)
        }
        val config = DefaultDatabaseConfig(proxy, dialect)
        return Database.create(object : DatabaseConfig by config {
            override val clockProvider: ClockProvider = clockProvider
            override val jdbcOption: JdbcOption = jdbcOption
        })
    }
}
