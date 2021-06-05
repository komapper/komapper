package integration.r2dbc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SchemaDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: R2dbcDatabase) {

    private val metamodels = listOf(Aaa.meta, Bbb.meta, Ccc.meta, CompositeKeyAddress.meta)

    @Test
    fun create() = inTransaction(db) {
        db.runQuery {
            SchemaDsl.create(metamodels)
        }
    }

    @Test
    fun drop() = inTransaction(db) {
        db.runQuery {
            SchemaDsl.create(metamodels)
        }
        db.runQuery {
            SchemaDsl.drop(metamodels)
        }
    }
}
