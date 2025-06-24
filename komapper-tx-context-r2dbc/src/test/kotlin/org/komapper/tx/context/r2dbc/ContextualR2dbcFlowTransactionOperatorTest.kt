package org.komapper.tx.context.r2dbc

import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.flow.collect
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

internal class ContextualR2dbcFlowTransactionOperatorTest {
    private val connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///transaction-test;DB_CLOSE_DELAY=-1")
    private val config = object : DefaultR2dbcDatabaseConfig(connectionFactory, H2R2dbcDialect()) {
        override val session: R2dbcSession by lazy {
            R2dbcTransactionSession(connectionFactory, loggerFacade)
        }
    }
    private val db = R2dbcDatabase(config).asContextualDatabase()

    context(ctx: A)
    fun <A> implicit(): A = ctx

    @Test
    fun contextPropagation() = runBlocking {
        val a = Meta.address
        db.flowTransaction<Unit> {
            contextPropagated()
        }.collect()
        db.flowTransaction<Unit> {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("TOKYO", address.street)
        }.collect()
    }

    context(r2dbcContext: R2dbcContext)
    private suspend fun contextPropagated() {
        val a = Meta.address
        val tx = r2dbcContext.transaction
        assertNotNull(tx)
        assertFalse(tx.isRollbackOnly)
        val address = r2dbcContext.database.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        r2dbcContext.database.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
    }

    @Test
    fun commit() = runBlocking {
        val a = Meta.address
        db.flowTransaction<Unit> {
            assertFalse(implicit<R2dbcContext>().transactionOperator.isRollbackOnly())
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
            Unit
        }.collect()
        db.flowTransaction<Unit> {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("TOKYO", address.street)
        }.collect()
    }

    @Test
    fun setRollbackOnly() = runBlocking {
        val a = Meta.address
        db.flowTransaction<Unit> {
            implicit<R2dbcContext>().transactionOperator.setRollbackOnly()
            assertTrue(implicit<R2dbcContext>().transactionOperator.isRollbackOnly())
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
            Unit
        }.collect()
        db.flowTransaction<Unit> {
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            assertEquals("STREET 1", address.street)
        }.collect()
    }

    @BeforeTest
    fun before() {
        val sql =
            """
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
