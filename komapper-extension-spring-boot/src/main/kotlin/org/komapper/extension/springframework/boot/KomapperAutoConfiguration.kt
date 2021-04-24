package org.komapper.extension.springframework.boot

import org.komapper.core.Database
import org.komapper.core.Dialect
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

@Configuration
@ConditionalOnClass(Database::class)
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
open class KomapperAutoConfiguration {

    companion object {
        private const val DATASOURCE_URL_PROPERTY = "spring.datasource.url"
    }

    @Suppress("unused")
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

    @Suppress("unused")
    @Bean
    @ConditionalOnMissingBean(Database::class)
    open fun database(dataSource: DataSource, dialect: Dialect): Database {
        val proxy = if (dataSource is TransactionAwareDataSourceProxy) {
            dataSource
        } else {
            TransactionAwareDataSourceProxy(dataSource)
        }
        return Database(proxy, dialect)
    }
}
