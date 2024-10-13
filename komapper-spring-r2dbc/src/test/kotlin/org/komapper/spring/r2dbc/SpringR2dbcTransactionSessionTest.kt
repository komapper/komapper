package org.komapper.spring.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.runBlocking
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.dialect.h2.r2dbc.H2R2dbcDialect
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcSession
import org.reactivestreams.Publisher
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpringR2dbcTransactionSessionTest {
    @Volatile
    private var openCount = 0

    @Volatile
    private var closeCount = 0

    private val connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///transaction-test;DB_CLOSE_DELAY=-1").let { factory ->
        object : ConnectionFactory by factory {
            override fun create(): Publisher<out Connection> {
                openCount++
                return factory.create().asFlow().map { connection ->
                    object : Connection by connection {
                        override fun close(): Publisher<Void> {
                            closeCount++
                            return connection.close()
                        }
                    }
                }.asPublisher()
            }
        }
    }
    private val transactionManager = R2dbcTransactionManager(connectionFactory)
    private val config = object : DefaultR2dbcDatabaseConfig(connectionFactory, H2R2dbcDialect()) {
        override val session: R2dbcSession = SpringR2dbcTransactionSession(transactionManager, connectionFactory)
    }
    private val db = R2dbcDatabase(config)

    @Test
    fun `verify that the connection is always closed when transactions are not enabled`() = runBlocking {
        assertEquals(1, openCount)
        assertEquals(1, closeCount)
        val a = Meta.address
        val singleQuery = QueryDsl.from(a).where { a.addressId eq 1 }.single()
        val address = db.runQuery { singleQuery }
        db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
        val address2 = db.runQuery { singleQuery }
        assertEquals("TOKYO", address2.street)
        assertEquals(4, openCount)
        assertEquals(4, closeCount)
    }

    @BeforeTest
    fun before() {
        val sql = """
            CREATE TABLE ADDRESS(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
            INSERT INTO ADDRESS VALUES(1,'STREET 1',1);
            INSERT INTO ADDRESS VALUES(2,'STREET 2',1);
            INSERT INTO ADDRESS VALUES(3,'STREET 3',1);
            INSERT INTO ADDRESS VALUES(4,'STREET 4',1);
            INSERT INTO ADDRESS VALUES(5,'STREET 5',1);
            INSERT INTO ADDRESS VALUES(6,'STREET 6',1);
            INSERT INTO ADDRESS VALUES(7,'STREET 7',1);
            INSERT INTO ADDRESS VALUES(8,'STREET 8',1);
            INSERT INTO ADDRESS VALUES(9,'STREET 9',1);
            INSERT INTO ADDRESS VALUES(10,'STREET 10',1);
            INSERT INTO ADDRESS VALUES(11,'STREET 11',1);
            INSERT INTO ADDRESS VALUES(12,'STREET 12',1);
            INSERT INTO ADDRESS VALUES(13,'STREET 13',1);
            INSERT INTO ADDRESS VALUES(14,'STREET 14',1);
            INSERT INTO ADDRESS VALUES(15,'STREET 15',1);
        """.trimIndent()

        runBlocking {
            db.runQuery {
                QueryDsl.executeScript(sql)
            }
        }
    }

    @AfterTest
    fun after() {
        val sql = "DROP ALL OBJECTS"
        runBlocking {
            db.runQuery {
                QueryDsl.executeScript(sql)
            }
        }
    }
}
