package integration.jdbc

import integration.core.Address
import integration.core.Person
import integration.core.address
import integration.core.person
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityNotFoundException
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(JdbcEnv::class)
class JdbcDeleteSingleTest(private val db: JdbcDatabase) {
    @Test
    fun optimisticLockException() {
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
    fun testEntity() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        db.runQuery { QueryDsl.delete(a).single(address) }
        assertEquals(emptyList<Address>(), db.runQuery { query })
    }

    @Test
    fun throwEntityNotFoundException() {
        val p = Meta.person
        val person = Person(1, "aaa")
        val ex = assertFailsWith<EntityNotFoundException> {
            db.runQuery { QueryDsl.delete(p).single(person) }
        }
        println(ex)
    }

    @Test
    fun suppressEntityNotFoundException() {
        val p = Meta.person
        val person = Person(1, "aaa")
        db.runQuery {
            QueryDsl.delete(p).single(person).options {
                it.copy(suppressEntityNotFoundException = true)
            }
        }
    }
}
