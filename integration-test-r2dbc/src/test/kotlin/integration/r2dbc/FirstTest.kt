package integration.r2dbc

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl
import org.komapper.tx.r2dbc.transaction

@ExtendWith(Env::class)
class FirstTest(private val db: R2dbcDatabase) {

    @Test
    fun list() = runBlocking {
        val list = db.transaction {
            setRollbackOnly()
            val flow = db.runQuery {
                R2dbcEntityDsl.from(Address.meta)
            }
            flow.toList(mutableListOf())
        }
        assertEquals(15, list.size)
    }

    @Test
    fun where() = runBlocking {
        val list = db.transaction {
            setRollbackOnly()
            val flow = db.runQuery {
                val a = Address.meta
                R2dbcEntityDsl.from(Address.meta).where { a.addressId eq 1 }
            }
            flow.toList(mutableListOf())
        }
        assertEquals(1, list.size)
    }

    @Test
    fun delete() = runBlocking {
        db.transaction {
            setRollbackOnly()
            db.runQuery {
                val address = Address(15, "", 1)
                R2dbcEntityDsl.delete(Address.meta).single(address)
            }
        }.let { }
    }

    @Test
    fun requiresNew() = runBlocking {
        db.transaction {
            setRollbackOnly()
            val flow = db.runQuery {
                val a = Address.meta
                R2dbcEntityDsl.from(Address.meta).where { a.addressId eq 15 }
            }
            assertNotNull(flow.firstOrNull())
            requiresNew {
                setRollbackOnly()
                db.runQuery {
                    val address = Address(15, "", 1)
                    R2dbcEntityDsl.delete(Address.meta).single(address)
                }
            }
            val flow2 = db.runQuery {
                val a = Address.meta
                R2dbcEntityDsl.from(Address.meta).where { a.addressId eq 15 }
            }
            assertNotNull(flow2.firstOrNull())
        }.let { }
    }

    @Test
    fun required() = runBlocking {
        db.transaction {
            setRollbackOnly()
            val flow = db.runQuery {
                val a = Address.meta
                R2dbcEntityDsl.from(Address.meta).where { a.addressId eq 15 }
            }
            assertNotNull(flow.firstOrNull())
            required {
                setRollbackOnly()
                db.runQuery {
                    val address = Address(15, "", 1)
                    R2dbcEntityDsl.delete(Address.meta).single(address)
                }
            }
            val flow2 = db.runQuery {
                val a = Address.meta
                R2dbcEntityDsl.from(Address.meta).where { a.addressId eq 15 }
            }
            assertNull(flow2.firstOrNull())
        }.let { }
    }
}
