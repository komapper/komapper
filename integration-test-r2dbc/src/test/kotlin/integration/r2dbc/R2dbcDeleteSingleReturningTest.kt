package integration.r2dbc

import integration.core.Dbms
import integration.core.Person
import integration.core.Run
import integration.core.address
import integration.core.person
import integration.core.site
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityNotFoundException
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(R2dbcEnv::class)
class R2dbcDeleteSingleReturningTest(private val db: R2dbcDatabase) {
    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun optimisticLockException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 15
            }.first()
        }
        db.runQuery { QueryDsl.delete(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.delete(a).single(address).returning() }
            Unit
        }
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val address2 = db.runQuery { QueryDsl.delete(a).single(address).returning() }
        assertEquals(address, address2)
        val address3 = db.runQuery { query.firstOrNull() }
        assertNull(address3)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningSingleColumn(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val street = db.runQuery { QueryDsl.delete(a).single(address).returning(a.street) }
        assertEquals(address.street, street)
        val address2 = db.runQuery { query.firstOrNull() }
        assertNull(address2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningPairColumns(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val pair = db.runQuery { QueryDsl.delete(a).single(address).returning(a.street, a.version) }
        assertEquals(address.street to address.version, pair)
        val address2 = db.runQuery { query.firstOrNull() }
        assertNull(address2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningTripleColumns(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val triple = db.runQuery { QueryDsl.delete(a).single(address).returning(a.street, a.version, a.addressId) }
        assertEquals(Triple(address.street, address.version, address.addressId), triple)
        val address2 = db.runQuery { query.firstOrNull() }
        assertNull(address2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningSingleColumn_null(info: TestInfo) = inTransaction(db, info) {
        val s = Meta.site
        val query = QueryDsl.from(s).where { s.id eq 15 }
        val site = db.runQuery { query.first() }
        db.runQuery { QueryDsl.update(s).single(site.copy(street = null)) }
        val site2 = db.runQuery { query.first() }
        assertNull(site2.street)
        val street = db.runQuery { QueryDsl.delete(s).single(site2).returning(s.street) }
        assertNull(street)
        val site3 = db.runQuery { query.firstOrNull() }
        assertNull(site3)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun unsupportedOperationException_deleteReturning(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.delete(a).single(address).returning() }
            Unit
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun throwEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val person = Person(1, "aaa")
        val ex = assertFailsWith<EntityNotFoundException> {
            db.runQuery { QueryDsl.delete(p).single(person).returning() }
            Unit
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun suppressEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val person = Person(1, "aaa")
        val result = db.runQuery {
            QueryDsl.delete(p).single(person).returning().options {
                it.copy(suppressEntityNotFoundException = true)
            }
        }
        assertNull(result)
    }
}
