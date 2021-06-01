package integration.r2dbc

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.komapper.dialect.h2.r2dbx.H2R2dbcDialect
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl
import org.komapper.tx.r2dbc.TransactionAttribute
import org.komapper.tx.r2dbc.transaction

class TransactionTest {

    private val connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///transaction-test;DB_CLOSE_DELAY=-1")
    private val config = DefaultR2dbcDatabaseConfig(connectionFactory, H2R2dbcDialect())
    private val db = R2dbcDatabase.create(config)

    @BeforeEach
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
            val con = connectionFactory.create().awaitSingle()
            val batch = con.createBatch()
            for (each in sql.split(";")) {
                batch.add(each.trim())
            }
            batch.execute().awaitLast()
            con.close().awaitFirstOrNull()
        }
    }

    @AfterEach
    fun after() {
        val sql = "DROP ALL OBJECTS"
        runBlocking {
            val con = connectionFactory.create().awaitSingle()
            val statement = con.createStatement(sql)
            val result = statement.execute().awaitSingle()
            result.rowsUpdated.awaitSingle()
            con.close().awaitFirstOrNull()
        }
    }

    @Test
    fun select() = runBlocking {
        val a = Address.meta
        val list = db.transaction {
            db.runQuery { R2dbcEntityDsl.from(a) }.toList()
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun commit() = runBlocking {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction {
            val address = db.runQuery { query.first() }
            db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
        }
        db.transaction {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun rollback() = runBlocking {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        try {
            db.transaction {
                val address = db.runQuery { query.first() }
                db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
                throw Exception()
            }
        } catch (ignored: Exception) {
        }
        db.transaction {
            val address = db.runQuery { query.first() }
            assertNotNull(address)
        }
    }

    @Test
    fun setRollbackOnly() = runBlocking {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction {
            val address = db.runQuery { query.first() }
            db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
            assertFalse(isRollbackOnly())
            setRollbackOnly()
            assertTrue(isRollbackOnly())
        }
        db.transaction {
            val address = db.runQuery { query.first() }
            assertNotNull(address)
        }
    }

    @Test
    fun isolationLevel() = runBlocking {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction(isolationLevel = IsolationLevel.SERIALIZABLE) {
            val address = db.runQuery { query.first() }
            db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
        }
        db.transaction {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun required_required() = runBlocking {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction {
            val address = db.runQuery { query.first() }
            db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
            required {
                val address2 = db.runQuery { query.firstOrNull() }
                assertNull(address2)
            }
        }
        db.transaction {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun requiresNew() = runBlocking {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction(TransactionAttribute.REQUIRES_NEW) {
            val address = db.runQuery { query.first() }
            db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
            val address2 = db.runQuery { query.firstOrNull() }
            assertNull(address2)
        }
        db.transaction {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun required_requiresNew() = runBlocking {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction {
            val address = db.runQuery { query.first() }
            db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
            requiresNew {
                val address2 = db.runQuery { query.firstOrNull() }
                assertNotNull(address2)
            }
        }
        db.transaction {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }
}
