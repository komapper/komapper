package org.komapper.tx.r2dbc

import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.runBlocking
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.dialect.h2.r2dbc.H2R2dbcDialect
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcSession
import org.komapper.tx.core.TransactionAttribute
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MultipleDatabasesTest {
    private val connectionFactory1 = ConnectionFactories.get("r2dbc:h2:mem:///transaction-test1;DB_CLOSE_DELAY=-1")
    private val config1 = object : DefaultR2dbcDatabaseConfig(connectionFactory1, H2R2dbcDialect()) {
        override val session: R2dbcSession by lazy {
            R2dbcTransactionSession(connectionFactory, loggerFacade)
        }
    }
    private val db1 = R2dbcDatabase(config1)

    private val connectionFactory2 = ConnectionFactories.get("r2dbc:h2:mem:///transaction-test2;DB_CLOSE_DELAY=-1")
    private val config2 = object : DefaultR2dbcDatabaseConfig(connectionFactory2, H2R2dbcDialect()) {
        override val session: R2dbcSession by lazy {
            R2dbcTransactionSession(connectionFactory2, loggerFacade)
        }
    }
    private val db2 = R2dbcDatabase(config2)

    @Test
    fun required() = runBlocking {
        db1.withTransaction(TransactionAttribute.REQUIRED) {
            db1.runQuery {
                QueryDsl.insert(Meta.address).single(Address(addressId = 1, street = "a"))
            }
            db2.withTransaction(TransactionAttribute.REQUIRED) {
                db2.runQuery {
                    QueryDsl.insert(Meta.person).single(Person(personId = 1, name = "b"))
                }
            }
        }
        val address = db1.runQuery {
            QueryDsl.from(Meta.address).single()
        }
        assertEquals(Address(1, "a", 0), address)
        val person = db2.runQuery {
            QueryDsl.from(Meta.person).single()
        }
        assertEquals(Person(1, "b", 0), person)
    }

    @BeforeTest
    fun before() {
        runBlocking {
            db1.runQuery {
                QueryDsl.executeScript(
                    """
                    CREATE TABLE ADDRESS(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
                    """.trimIndent(),
                )
            }
            db2.runQuery {
                QueryDsl.executeScript(
                    """
                    CREATE TABLE PERSON(PERSON_ID INTEGER NOT NULL PRIMARY KEY, NAME VARCHAR(20) UNIQUE, VERSION INTEGER);
                    """.trimIndent(),
                )
            }
        }
    }

    @AfterTest
    fun after() {
        runBlocking {
            db1.runQuery {
                QueryDsl.executeScript("DROP ALL OBJECTS")
            }
            db2.runQuery {
                QueryDsl.executeScript("DROP ALL OBJECTS")
            }
        }
    }
}
