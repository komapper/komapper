package integration.r2dbc

import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.identityStrategy
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcInsertSelectTest(private val db: R2dbcDatabase) {
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val aa = a.clone(table = "address_archive")
        val (count, ids) = db.runQuery {
            val query = QueryDsl.from(a).where { a.addressId between 1..5 }
            QueryDsl.insert(aa).select(query)
        }
        assertEquals(5, count)
        assertEquals(emptyList(), ids)
    }

    @Test
    fun test_lambda(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val aa = a.clone(table = "address_archive")
        val (count, ids) = db.runQuery {
            QueryDsl.insert(aa).select {
                QueryDsl.from(a).where { a.addressId between 1..5 }
            }
        }
        assertEquals(5, count)
        assertEquals(emptyList(), ids)
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5, Dbms.MARIADB, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun generatedKeys(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        db.runQuery {
            val q1 = QueryDsl.insert(i).values {
                i.value eq "test"
            }
            val q2 = QueryDsl.insert(i).values {
                i.value eq "test2"
            }
            q1.andThen(q2)
        }
        val (count, ids) = db.runQuery {
            QueryDsl.insert(i).select {
                QueryDsl.from(i)
            }
        }
        assertEquals(2, count)
        assertEquals(listOf(3, 4), ids)
    }

    @Run(onlyIf = [Dbms.MYSQL, Dbms.MYSQL_5, Dbms.MARIADB, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun generatedKeys_unsupportedOperationException(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        db.runQuery {
            val q1 = QueryDsl.insert(i).values {
                i.value eq "test"
            }
            val q2 = QueryDsl.insert(i).values {
                i.value eq "test2"
            }
            q1.andThen(q2)
        }
        assertFailsWith<UnsupportedOperationException> {
            db.runQuery {
                QueryDsl.insert(i).select {
                    QueryDsl.from(i)
                }
            }
            Unit
        }
    }

    @Run(unless = [Dbms.ORACLE])
    @Test
    fun generatedKeys_doNotReturnGeneratedKeys(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        db.runQuery {
            val q1 = QueryDsl.insert(i).values {
                i.value eq "test"
            }
            val q2 = QueryDsl.insert(i).values {
                i.value eq "test2"
            }
            q1.andThen(q2)
        }
        val (count, ids) = db.runQuery {
            QueryDsl.insert(i).select {
                QueryDsl.from(i)
            }.options {
                it.copy(returnGeneratedKeys = false)
            }
        }
        assertEquals(2, count)
        assertTrue(ids.isEmpty())
    }
}
