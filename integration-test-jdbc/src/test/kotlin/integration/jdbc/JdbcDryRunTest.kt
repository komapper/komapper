package integration.jdbc

import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dryRunQuery
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.stringNotNull
import org.komapper.jdbc.JdbcDatabase
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcDryRunTest(private val db: JdbcDatabase) {
    @Test
    fun dryRunQuery() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 1 }
        val result = db.dryRunQuery(query)
        assertNull(result.throwable)
    }

    @Test
    fun dryRunQuery_block() {
        val result = db.dryRunQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 1 }
        }
        assertNull(result.throwable)
    }

    @Test
    fun dryRunQuery_error() {
        val query = QueryDsl
            .fromTemplate("SELECT street FROM address WHERE address_id = /* unknown */''")
            .select { it.stringNotNull(0) }
        val result = db.dryRunQuery(query)
        assertNotNull(result.throwable)
        val writer = StringWriter()
        result.throwable!!.printStackTrace(PrintWriter(writer))
        val message = writer.toString()
        assertEquals(message, result.sql)
        assertEquals(message, result.sqlWithArgs)
    }
}
