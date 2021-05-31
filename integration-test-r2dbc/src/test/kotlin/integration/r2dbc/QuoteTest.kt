package integration.r2dbc

import integration.r2dbc.setting.Dbms
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl

@ExtendWith(Env::class)
class QuoteTest(val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogAndSchema() = runBlocking {
        val m = CatalogAndSchema.meta
        val query = R2dbcEntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "catalog"."schema"."CATALOG_AND_SCHEMA" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun catalogOnly() = runBlocking {
        val m = CatalogOnly.meta
        val query = R2dbcEntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "catalog"."CATALOG_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun schemaOnly() = runBlocking {
        val m = SchemaOnly.meta
        val query = R2dbcEntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "schema"."SCHEMA_ONLY" """))
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun blankName() = runBlocking {
        val m = BlankName.meta
        val query = R2dbcEntityDsl.from(m)
        val sql = query.dryRun()
        println(sql)
        assertTrue(sql.contains(""" "BLANK_NAME" """))
        assertTrue(sql.contains("ID"))
    }

    @Test
    fun alwaysQuote() = inTransaction(db) {
        val m = Order.meta
        db.runQuery { R2dbcEntityDsl.insert(m).single(Order(1, "value")) }
        val list = db.runQuery { R2dbcEntityDsl.from(m) }.toList()
        assertEquals(1, list.size)
    }
}
