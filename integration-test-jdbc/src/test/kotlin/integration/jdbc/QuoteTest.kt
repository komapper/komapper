package integration.jdbc

import integration.Order
import integration.blankName
import integration.catalogAndSchema
import integration.catalogOnly
import integration.order
import integration.schemaOnly
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.dryRun
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class QuoteTest(val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogAndSchema() {
        val m = Meta.catalogAndSchema
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."catalog_and_schema" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() {
        val m = Meta.catalogOnly
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."catalog_only" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() {
        val m = Meta.schemaOnly
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "schema"."schema_only" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() {
        val m = Meta.blankName
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "blank_name" """))
        assertTrue(sql.contains("id"))
    }

    @Test
    fun alwaysQuote() {
        val m = Meta.order
        db.runQuery { QueryDsl.insert(m).single(Order(1, "value")) }
        val list = db.runQuery { QueryDsl.from(m) }
        assertEquals(1, list.size)
    }
}
