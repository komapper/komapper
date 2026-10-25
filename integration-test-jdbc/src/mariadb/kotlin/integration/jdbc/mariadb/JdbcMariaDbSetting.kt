package integration.jdbc.mariadb

import integration.core.MariaDbSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDatabase
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MariaDBContainerProvider
import org.testcontainers.jdbc.ConnectionUrl

@Suppress("unused")
class JdbcMariaDbSetting(url: String) : MariaDbSetting<JdbcDatabase> {
    override val database: JdbcDatabase by lazy {
        val connectionUrl = ConnectionUrl.newInstance(url)
        val container = MariaDBContainerProvider().newInstance(connectionUrl) as MariaDBContainer<*>
        // MariaDB Connector/J 4 enables TLS by default, but the container does not support it.
        // The parameter must be set on the container so that its startup check also uses it.
        container.withUrlParam("sslMode", "disable")
        container.start()
        JdbcDatabase(
            container.jdbcUrl,
            container.username,
            container.password,
            executionOptions = ExecutionOptions(batchSize = 2),
        )
    }
}
