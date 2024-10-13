package integration.jdbc

import integration.core.Address
import integration.core.CompositeKeyAddress
import integration.core.Department
import integration.core.address
import integration.core.compositeKeyAddress
import integration.core.department
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcInsertExecAndGetTest(private val db: JdbcDatabase) {
    @Test
    fun onDuplicateKeyUpdate_insert() {
        val d = Meta.department
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().executeAndGet(department)
        val department2 = db.runQuery { query }
        assertEquals(department, department2)
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertEquals(department2, found)
    }

    @Test
    fun onDuplicateKeyUpdate_update() {
        val d = Meta.department
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().executeAndGet(department)
        val department2 = db.runQuery { query }
        assertEquals(department, department2)
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 1 }.first() }
        assertEquals(department2, found)
    }

    @Test
    fun onDuplicateKeyUpdate_insert_composite_key() {
        val a = Meta.compositeKeyAddress
        val address = CompositeKeyAddress(15, 15, "STREET", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyUpdate().executeAndGet(address)
        val address2 = db.runQuery { query }
        assertEquals(address, address2)
        val found = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId1 eq 15
                a.addressId2 eq 15
            }.first()
        }
        assertEquals(address2, found)
    }

    @Test
    fun onDuplicateKeyUpdate_update_composite_key() {
        val a = Meta.compositeKeyAddress
        val address = CompositeKeyAddress(1, 1, "STREET X", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyUpdate().executeAndGet(address)
        val address2 = db.runQuery { query }
        assertEquals(address, address2)
        val found = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId1 eq 1
                a.addressId2 eq 1
            }.first()
        }
        assertEquals(address2, found)
    }

    @Test
    fun onDuplicateKeyUpdate_withKeys_insert() {
        val d = Meta.department
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).executeAndGet(department)
        val department2 = db.runQuery { query }
        assertEquals(department, department2)
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertEquals(department2, found)
    }

    @Test
    fun onDuplicateKeyUpdate_withKeys_update() {
        val d = Meta.department
        val department = Department(6, 10, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).executeAndGet(department)
        val department2 = db.runQuery { query }
        assertNotEquals(department, department2)
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(department2, found)
    }

    @Test
    fun onDuplicateKeyIgnore_insert() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().executeAndGet(address)
        val address2 = db.runQuery { query }
        assertEquals(address, address2)
    }

    @Test
    fun onDuplicateKeyIgnore_ignore() {
        val a = Meta.address
        val address = Address(1, "STREET 100", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().executeAndGet(address)
        val address2 = db.runQuery { query }
        assertNull(address2)
    }

    @Test
    fun onDuplicateKeyIgnore_withKeys_ignore() {
        val a = Meta.address
        val address = Address(100, "STREET 1", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore(a.street).executeAndGet(address)
        val address2 = db.runQuery { query }
        assertNull(address2)
    }
}
