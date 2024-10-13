package org.komapper.tx.context.r2dbc

import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.runBlocking
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.dialect.h2.r2dbc.H2R2dbcDialect
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcSession
import org.komapper.tx.r2dbc.R2dbcTransactionSession
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ContextualR2dbcCoroutineTransactionOperatorTest {
    private val connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///transaction-test;DB_CLOSE_DELAY=-1")
    private val config = object : DefaultR2dbcDatabaseConfig(connectionFactory, H2R2dbcDialect()) {
        override val session: R2dbcSession by lazy {
            R2dbcTransactionSession(connectionFactory, loggerFacade)
        }
    }
    private val db = R2dbcDatabase(config).asContextualDatabase()

    @Test
    fun contextPropagation() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            contextPropagated()
        }
        db.withTransaction {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("TOKYO", address.street)
        }
    }

    context(R2dbcContext)
    private suspend fun contextPropagated() {
        val a = Meta.address
        val tx = transaction
        assertNotNull(tx)
        assertFalse(tx.isRollbackOnly)
        val address = database.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        database.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
    }

    @Test
    fun commit() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            assertFalse(transactionOperator.isRollbackOnly())
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
        }
        db.withTransaction {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("TOKYO", address.street)
        }
    }

    @Test
    fun setRollbackOnly() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            transactionOperator.setRollbackOnly()
            assertTrue(transactionOperator.isRollbackOnly())
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
        }
        db.withTransaction {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("STREET 1", address.street)
        }
    }

    @Test
    fun setRollbackOnly_required() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            transactionOperator.setRollbackOnly()
            assertTrue(transactionOperator.isRollbackOnly())
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            transactionOperator.required {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                Unit
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("STREET 1", address1.street)
            assertEquals("STREET 2", address2.street)
        }
    }

    @Test
    fun setRollbackOnly_requiresNew() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            transactionOperator.setRollbackOnly()
            assertTrue(transactionOperator.isRollbackOnly())
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            transactionOperator.requiresNew {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                Unit
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("STREET 1", address1.street)
            assertEquals("OSAKA", address2.street)
        }
    }

    @Test
    fun throwRuntimeException() = runBlocking {
        val a = Meta.address
        try {
            db.withTransaction {
                val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
                db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
                throw RuntimeException()
            }
        } catch (ignored: Exception) {
        }
        db.withTransaction {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("STREET 1", address.street)
        }
    }

    @Test
    fun throwException() = runBlocking {
        val a = Meta.address
        try {
            db.withTransaction {
                val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
                db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
                throw Exception()
            }
        } catch (ignored: Exception) {
        }
        db.withTransaction {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("STREET 1", address.street)
        }
    }

    @Test
    fun required_commit() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            transactionOperator.required {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("TOKYO", address1.street)
            assertEquals("OSAKA", address2.street)
        }
    }

    @Test
    fun required_setRollbackOnly() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            transactionOperator.required {
                transactionOperator.setRollbackOnly()
                assertTrue(transactionOperator.isRollbackOnly())
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("STREET 1", address1.street)
            assertEquals("STREET 2", address2.street)
        }
    }

    @Test
    fun required_throwRuntimeException() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                transactionOperator.required {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw RuntimeException()
                }
            } catch (ignored: Exception) {
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("TOKYO", address1.street)
            assertEquals("OSAKA", address2.street)
        }
    }

    @Test
    fun required_throwException() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                transactionOperator.required {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw Exception()
                }
            } catch (ignored: Exception) {
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("TOKYO", address1.street)
            assertEquals("OSAKA", address2.street)
        }
    }

    @Test
    fun requiresNew_commit() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            transactionOperator.requiresNew {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("TOKYO", address1.street)
            assertEquals("OSAKA", address2.street)
        }
    }

    @Test
    fun requiresNew_setRollbackOnly() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            transactionOperator.requiresNew {
                transactionOperator.setRollbackOnly()
                assertTrue(transactionOperator.isRollbackOnly())
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("TOKYO", address1.street)
            assertEquals("STREET 2", address2.street)
        }
    }

    @Test
    fun requiresNew_throwRuntimeException() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                transactionOperator.requiresNew {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw RuntimeException()
                }
            } catch (ignored: Exception) {
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("TOKYO", address1.street)
            assertEquals("STREET 2", address2.street)
        }
    }

    @Test
    fun requiresNew_throwException() = runBlocking {
        val a = Meta.address
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                transactionOperator.requiresNew {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw Exception()
                }
            } catch (ignored: Exception) {
            }
        }
        db.withTransaction {
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
            assertEquals("TOKYO", address1.street)
            assertEquals("STREET 2", address2.street)
        }
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
            db.withTransaction {
                db.runQuery {
                    QueryDsl.executeScript(sql)
                }
            }
        }
    }

    @AfterTest
    fun after() {
        val sql = "DROP ALL OBJECTS"
        runBlocking {
            db.withTransaction {
                db.runQuery {
                    QueryDsl.executeScript(sql)
                }
            }
        }
    }
}
