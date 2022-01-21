package integration.jdbc

import integration.Address
import integration.address
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class SelectForUpdateTest(private val db: JdbcDatabase) {

    // TODO
    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun forUpdate() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId greaterEq 1 }
                .orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
                .forUpdate()
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }
}
