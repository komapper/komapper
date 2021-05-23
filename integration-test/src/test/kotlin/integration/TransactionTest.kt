package integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.komapper.jdbc.Database
import org.komapper.jdbc.DefaultDatabaseConfig
import org.komapper.jdbc.SimpleDataSource
import org.komapper.jdbc.dsl.EntityDsl
import org.komapper.jdbc.dsl.runQuery
import org.komapper.jdbc.h2.H2JdbcDialect
import org.komapper.transaction.TransactionIsolationLevel
import org.komapper.transaction.transaction

class TransactionTest {

    private val dataSource = SimpleDataSource("jdbc:h2:mem:transaction-test;DB_CLOSE_DELAY=-1")
    private val config = DefaultDatabaseConfig(dataSource, H2JdbcDialect())
    private val db = Database.create(config)

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

        dataSource.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }

    @AfterEach
    fun after() {
        val sql = "DROP ALL OBJECTS"
        dataSource.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }

    @Test
    fun select() {
        val a = Address.meta
        val list = db.transaction.required {
            db.runQuery { EntityDsl.from(a) }
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun commit() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction.required {
            val address = db.runQuery { query.first() }
            db.runQuery { EntityDsl.delete(a).single(address) }
        }
        db.transaction.required {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun rollback() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        try {
            db.transaction.required {
                val address = db.runQuery { query.first() }
                db.runQuery { EntityDsl.delete(a).single(address) }
                throw Exception()
            }
        } catch (ignored: Exception) {
        }
        db.transaction.required {
            val address = db.runQuery { query.first() }
            assertNotNull(address)
        }
    }

    @Test
    fun setRollbackOnly() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction.required {
            val address = db.runQuery { query.first() }
            db.runQuery { EntityDsl.delete(a).single(address) }
            assertFalse(isRollbackOnly())
            setRollbackOnly()
            assertTrue(isRollbackOnly())
        }
        db.transaction.required {
            val address = db.runQuery { query.first() }
            assertNotNull(address)
        }
    }

    @Test
    fun isolationLevel() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction.required(TransactionIsolationLevel.SERIALIZABLE) {
            val address = db.runQuery { query.first() }
            db.runQuery { EntityDsl.delete(a).single(address) }
        }
        db.transaction.required {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun required_required() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction.required {
            val address = db.runQuery { query.first() }
            db.runQuery { EntityDsl.delete(a).single(address) }
            required {
                val address2 = db.runQuery { query.firstOrNull() }
                assertNull(address2)
            }
        }
        db.transaction.required {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun requiresNew() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction.requiresNew {
            val address = db.runQuery { query.first() }
            db.runQuery { EntityDsl.delete(a).single(address) }
            val address2 = db.runQuery { query.firstOrNull() }
            assertNull(address2)
        }
        db.transaction.required {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun required_requiresNew() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction.required {
            val address = db.runQuery { query.first() }
            db.runQuery { EntityDsl.delete(a).single(address) }
            requiresNew {
                val address2 = db.runQuery { query.firstOrNull() }
                assertNotNull(address2)
            }
        }
        db.transaction.required {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }

    @Test
    fun invoke() {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        db.transaction {
            val address = db.runQuery { query.first() }
            db.runQuery { EntityDsl.delete(a).single(address) }
        }
        db.transaction {
            val address = db.runQuery { query.firstOrNull() }
            assertNull(address)
        }
    }
}
