package integration.jdbc

import integration.BlankName
import integration.CatalogAndSchema
import integration.CatalogOnly
import integration.Order
import integration.SchemaOnly
import integration.meta
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
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
        val m = CatalogAndSchema.meta
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() {
        val m = CatalogOnly.meta
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() {
        val m = SchemaOnly.meta
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() {
        val m = BlankName.meta
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
        assertTrue(sql.contains("ID"))
    }

    @Test
    fun alwaysQuote() {
        val m = Order.meta
        db.runQuery { QueryDsl.insert(m).single(Order(1, "value")) }
        val list = db.runQuery { QueryDsl.from(m) }
        assertEquals(1, list.size)
    }
}
