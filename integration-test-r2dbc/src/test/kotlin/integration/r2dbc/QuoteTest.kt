package integration.r2dbc

import integration.Order
import integration.blankName
import integration.catalogAndSchema
import integration.catalogOnly
import integration.order
import integration.schemaOnly
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.dryRun
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("lowPriority")
@ExtendWith(Env::class)
class QuoteTest(val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogAndSchema() = runBlockingWithTimeout {
        val m = Meta.catalogAndSchema
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."catalog_and_schema" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() = runBlockingWithTimeout {
        val m = Meta.catalogOnly
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."catalog_only" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() = runBlockingWithTimeout {
        val m = Meta.schemaOnly
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "schema"."schema_only" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() = runBlockingWithTimeout {
        val m = Meta.blankName
        val query = QueryDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "blank_name" """))
        assertTrue(sql.contains("id"))
    }

    @Test
    fun alwaysQuote() = inTransaction(db) {
        val m = Meta.order
        db.runQuery { QueryDsl.insert(m).single(Order(1, "value")) }
        val list = db.runQuery { QueryDsl.from(m) }
        assertEquals(1, list.size)
    }
}
