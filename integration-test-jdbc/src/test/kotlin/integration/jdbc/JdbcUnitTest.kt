package integration.jdbc

import integration.core.MyMeta
import integration.core.myAddress
import integration.core.myPerson
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class JdbcUnitTest(private val db: JdbcDatabase) {
    @Test
    fun test() {
        val a = MyMeta.myAddress
        val p = MyMeta.myPerson
        val addressList = db.runQuery { QueryDsl.from(a) }
        val personList = db.runQuery { QueryDsl.from(p) }
        assertEquals(15, addressList.size)
        assertEquals(0, personList.size)
    }
}
