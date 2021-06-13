package integration.r2dbc

import integration.BlankName
import integration.CatalogAndSchema
import integration.CatalogOnly
import integration.Order
import integration.SchemaOnly
import integration.meta
import integration.setting.Dbms
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.query.dryRun
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class QuoteTest(val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogAndSchema() = runBlocking {
        val m = CatalogAndSchema.meta
        val query = EntityDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() = runBlocking {
        val m = CatalogOnly.meta
        val query = EntityDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() = runBlocking {
        val m = SchemaOnly.meta
        val query = EntityDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() = runBlocking {
        val m = BlankName.meta
        val query = EntityDsl.from(m)
        val sql = query.dryRun().sql
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
        assertTrue(sql.contains("ID"))
    }

    @Test
    fun alwaysQuote() = inTransaction(db) {
        val m = Order.meta
        db.runQuery { EntityDsl.insert(m).single(Order(1, "value")) }
        val list = db.runQuery { EntityDsl.from(m) }.toList()
        assertEquals(1, list.size)
    }
}
