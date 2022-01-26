package integration.jdbc

import integration.address
import integration.identityStrategy
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class InsertSelectTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
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
    fun test_lambda() {
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

    // TODO: SQL Server driver doesn't return all generated values after a multiple insert statement is issued
    // TODO: ORACLE driver does not support multiple insert when the identity column is used
    @Run(unless = [Dbms.SQLSERVER, Dbms.ORACLE])
    @Test
    fun generatedKeys() {
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
}
