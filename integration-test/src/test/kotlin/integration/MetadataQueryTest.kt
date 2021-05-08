package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.MetadataDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class MetadataQueryTest(val db: Database) {

    @Test
    fun test() {
        val table = db.runQuery {
            MetadataDsl.tables(tableNamePattern = "ADDRESS")
        }.first()
        println(table)
        assertEquals(3, table.columns.size)
    }
}
