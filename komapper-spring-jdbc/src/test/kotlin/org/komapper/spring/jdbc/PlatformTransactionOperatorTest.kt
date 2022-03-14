package org.komapper.spring.jdbc

import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.dialect.h2.jdbc.JdbcH2Dialect
import org.komapper.jdbc.DefaultJdbcDatabaseConfig
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.SimpleDataSource
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class PlatformTransactionOperatorTest {

    private val dataSource = SimpleDataSource("jdbc:h2:mem://transaction-test;DB_CLOSE_DELAY=-1")
    private val transactionManager = DataSourceTransactionManager(dataSource)
    private val config = object : DefaultJdbcDatabaseConfig(dataSource, JdbcH2Dialect()) {
        override val session: JdbcSession = PlatformTransactionSession(transactionManager, dataSource)
    }
    private val db = JdbcDatabase(config)

    @Test
    fun commit() {
        val a = Meta.address
        db.withTransaction { tx ->
            kotlin.test.assertFalse(tx.isRollbackOnly())
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
        }
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        kotlin.test.assertEquals("TOKYO", address.street)
    }

    @Test
    fun setRollbackOnly() {
        val a = Meta.address
        db.withTransaction { tx ->
            tx.setRollbackOnly()
            assertTrue(tx.isRollbackOnly())
            val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
        }
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        kotlin.test.assertEquals("STREET 1", address.street)
    }

    @Test
    fun setRollbackOnly_required() {
        val a = Meta.address
        db.withTransaction { tx ->
            tx.setRollbackOnly()
            assertTrue(tx.isRollbackOnly())
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            tx.required {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                Unit
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("STREET 1", address1.street)
        kotlin.test.assertEquals("STREET 2", address2.street)
    }

    @Test
    fun setRollbackOnly_requiresNew() {
        val a = Meta.address
        db.withTransaction { tx ->
            tx.setRollbackOnly()
            assertTrue(tx.isRollbackOnly())
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            tx.requiresNew {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                Unit
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("STREET 1", address1.street)
        kotlin.test.assertEquals("OSAKA", address2.street)
    }

    @Test
    fun throwRuntimeException() {
        val a = Meta.address
        try {
            db.withTransaction {
                val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
                db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
                throw RuntimeException()
            }
        } catch (ignored: Exception) {
        }
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        kotlin.test.assertEquals("STREET 1", address.street)
    }

    @Test
    fun throwException() {
        val a = Meta.address
        try {
            db.withTransaction {
                val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
                db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
                throw Exception()
            }
        } catch (ignored: Exception) {
        }
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        kotlin.test.assertEquals("STREET 1", address.street)
    }

    @Test
    fun required_commit() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            tx.required {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("TOKYO", address1.street)
        kotlin.test.assertEquals("OSAKA", address2.street)
    }

    @Test
    fun required_setRollbackOnly() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            tx.required { tx2 ->
                tx2.setRollbackOnly()
                assertTrue(tx2.isRollbackOnly())
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("STREET 1", address1.street)
        kotlin.test.assertEquals("STREET 2", address2.street)
    }

    @Test
    fun required_throwRuntimeException() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                tx.required {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw RuntimeException()
                }
            } catch (ignored: Exception) {
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("TOKYO", address1.street)
        kotlin.test.assertEquals("OSAKA", address2.street)
    }

    @Test
    fun required_throwException() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                tx.required {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw Exception()
                }
            } catch (ignored: Exception) {
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("TOKYO", address1.street)
        kotlin.test.assertEquals("OSAKA", address2.street)
    }

    @Test
    fun requiresNew_commit() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            tx.requiresNew {
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("TOKYO", address1.street)
        kotlin.test.assertEquals("OSAKA", address2.street)
    }

    @Test
    fun requiresNew_setRollbackOnly() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            tx.requiresNew { tx2 ->
                tx2.setRollbackOnly()
                assertTrue(tx2.isRollbackOnly())
                val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("TOKYO", address1.street)
        kotlin.test.assertEquals("STREET 2", address2.street)
    }

    @Test
    fun requiresNew_throwRuntimeException() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                tx.requiresNew {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw RuntimeException()
                }
            } catch (ignored: Exception) {
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("TOKYO", address1.street)
        kotlin.test.assertEquals("STREET 2", address2.street)
    }

    @Test
    fun requiresNew_throwException() {
        val a = Meta.address
        db.withTransaction { tx ->
            val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
            db.runQuery { QueryDsl.update(a).single(address1.copy(street = "TOKYO")) }
            try {
                tx.requiresNew {
                    val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
                    db.runQuery { QueryDsl.update(a).single(address2.copy(street = "OSAKA")) }
                    throw Exception()
                }
            } catch (ignored: Exception) {
            }
        }
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.single() }
        val address2 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 2 }.single() }
        kotlin.test.assertEquals("TOKYO", address1.street)
        kotlin.test.assertEquals("STREET 2", address2.street)
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

        db.runQuery {
            QueryDsl.executeScript(sql)
        }
    }

    @AfterTest
    fun after() {
        val sql = "DROP ALL OBJECTS"
        db.runQuery {
            QueryDsl.executeScript(sql)
        }
    }
}
