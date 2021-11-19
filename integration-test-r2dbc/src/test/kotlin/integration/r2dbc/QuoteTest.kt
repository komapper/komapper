package integration.r2dbc

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
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class QuoteTest(val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogAndSchema() = runBlockingWithTimeout {
        val m = Meta.catalogAndSchema
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() = runBlockingWithTimeout {
        val m = Meta.catalogOnly
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() = runBlockingWithTimeout {
        val m = Meta.schemaOnly
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() = runBlockingWithTimeout {
        val m = Meta.blankName
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
        assertTrue(sql.contains("ID"))
    }

    @Test
    fun alwaysQuote() = inTransaction(db) {
        val m = Meta.order
        db.runQuery { QueryDsl.insert(m).single(Order(1, "value")) }
        val list = db.runQuery { QueryDsl.from(m) }
        assertEquals(1, list.size)
    }
}
