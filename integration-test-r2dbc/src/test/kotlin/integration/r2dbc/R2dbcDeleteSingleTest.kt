package integration.r2dbc

import integration.core.Person
import integration.core.address
import integration.core.person
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityNotFoundException
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(R2dbcEnv::class)
class R2dbcDeleteSingleTest(private val db: R2dbcDatabase) {
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
            db.runQuery { QueryDsl.delete(a).single(address) }
        }
    }

    @Test
    fun testEntity(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { QueryDsl.delete(a).single(address) }
        assertNull(db.runQuery { query }.firstOrNull())
    }

    @Test
    fun throwEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val person = Person(1, "aaa")
        val ex = assertFailsWith<EntityNotFoundException> {
            db.runQuery { QueryDsl.delete(p).single(person) }
        }
        println(ex)
    }

    @Test
    fun suppressEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val person = Person(1, "aaa")
        db.runQuery {
            QueryDsl.delete(p).single(person).options {
                it.copy(suppressEntityNotFoundException = true)
            }
        }
    }
}
