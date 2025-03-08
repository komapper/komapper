package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.IdentityStrategy
import integration.core.Run
import integration.core.address
import integration.core.identityStrategy
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcSequentialExecutionTest(private val db: R2dbcDatabase) {
    // https://github.com/komapper/komapper/pull/1536
    @Test
    fun list(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).limit(3).orderBy(a.addressId)
        for (i in 1..1000) {
            val list: List<Address> = db.runQuery(query)
            assertEquals(3, list.size, "i=$i")
            assertEquals(listOf(1, 2, 3), list.map { it.addressId }.sorted(), "i=$i")
        }
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5, Dbms.MARIADB, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun identity(info: TestInfo) = inTransaction(db, info) {
        val id = Meta.identityStrategy
        for (i in 1..1000) {
            val strategies = listOf(
                IdentityStrategy(null, "AAA-$i"),
                IdentityStrategy(null, "BBB-$i"),
                IdentityStrategy(null, "CCC-$i"),
            )
            val results = db.runQuery { QueryDsl.insert(id).multiple(strategies) }.mapNotNull { it.id }
            assertEquals(3, results.size, "i=$i")
            assertEquals(results.sorted(), results, "i=$i")
        }
    }
}
