package org.komapper.tx.jdbc

import org.komapper.core.DefaultLoggerFacade
import org.komapper.core.StdOutLogger
import org.komapper.jdbc.JdbcIsolationLevel
import org.komapper.jdbc.SimpleDataSource
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class JdbcTransactionOperatorImplTest {

    data class Address(val id: Int, val street: String, val version: Int)

    class Repository(private val txManager: JdbcTransactionManager) {
        fun selectAll(): List<Address> {
            return txManager.dataSource.connection.use { con ->
                con.prepareStatement("select address_id, street, version from address order by address_id").use { ps ->
                    ps.executeQuery().use { rs ->
                        val list = mutableListOf<Address>()
                        while (rs.next()) {
                            val id = rs.getInt(1)
                            val street = rs.getString(2)
                            val version = rs.getInt(3)
                            val address = Address(id, street, version)
                            list.add(address)
                        }
                        list
                    }
                }
            }
        }

        fun selectById(id: Int): Address? {
            return selectAll().firstOrNull { it.id == id }
        }

        fun delete(id: Int): Int {
            return txManager.dataSource.connection.use { con ->
                con.prepareStatement("delete from address where address_id = ?").use { ps ->
                    ps.setInt(1, id)
                    ps.executeUpdate()
                }
            }
        }
    }

    private val dataSource = SimpleDataSource("jdbc:h2:mem:transaction-test;DB_CLOSE_DELAY=-1")
    private val txManager = JdbcTransactionManagerImpl(dataSource, DefaultLoggerFacade(StdOutLogger()))
    private val tx = JdbcTransactionOperatorImpl(txManager)
    private val repository = Repository(txManager)

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

        dataSource.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }

    @AfterTest
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
        val list = tx.required {
            repository.selectAll()
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun commit() {
        tx.required {
            repository.delete(15)
        }
        tx.required {
            val address = repository.selectById(15)
            assertNull(address)
        }
    }

    @Test
    fun rollback() {
        try {
            tx.required {
                repository.delete(15)
                throw Exception()
            }
        } catch (ignored: Exception) {
        }
        tx.required {
            val address = repository.selectById(15)
            assertNotNull(address)
        }
    }

    @Test
    fun setRollbackOnly() {
        tx.required { tx ->
            repository.delete(15)
            assertFalse(tx.isRollbackOnly())
            tx.setRollbackOnly()
            assertTrue(tx.isRollbackOnly())
        }
        tx.required {
            val address = repository.selectById(15)
            assertNotNull(address)
        }
    }

    @Test
    fun isolationLevel() {
        tx.required(transactionDefinition = JdbcIsolationLevel.SERIALIZABLE) {
            repository.delete(15)
        }
        tx.required {
            val address = repository.selectById(15)
            assertNull(address)
        }
    }

    @Test
    fun required_required() {
        tx.required {
            repository.delete(15)
            tx.required {
                val address = repository.selectById(15)
                assertNull(address)
            }
        }
        tx.required {
            val address = repository.selectById(15)
            assertNull(address)
        }
    }

    @Test
    fun requiresNew() {
        tx.requiresNew {
            repository.delete(15)
            val address = repository.selectById(15)
            assertNull(address)
        }
        tx.required {
            val address = repository.selectById(15)
            assertNull(address)
        }
    }

    @Test
    fun required_requiresNew() {
        tx.required { tx ->
            repository.delete(15)
            val address = repository.selectById(15)
            assertNull(address)
            tx.requiresNew {
                val address2 = repository.selectById(15)
                assertNotNull(address2)
            }
        }
        tx.required {
            val address = repository.selectById(15)
            assertNull(address)
        }
    }
}
