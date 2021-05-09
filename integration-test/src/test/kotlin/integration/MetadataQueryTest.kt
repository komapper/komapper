package integration

import integration.setting.Dbms
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.MetadataDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class MetadataQueryTest(val db: Database) {

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun test_uppercase() {
        val table = db.runQuery {
            MetadataDsl.tables(tableNamePattern = "ADDRESS")
        }.first()
        println(table)
        assertEquals(3, table.columns.size)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun test_lowercase() {
        val table = db.runQuery {
            MetadataDsl.tables(tableNamePattern = "address")
        }.first()
        println(table)
        assertEquals(3, table.columns.size)
    }
}
