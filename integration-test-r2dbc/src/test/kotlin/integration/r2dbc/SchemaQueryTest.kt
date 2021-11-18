package integration.r2dbc

import integration.aaa
import integration.bbb
import integration.ccc
import integration.compositeKeyAddress
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.SchemaDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: R2dbcDatabase) {

    private val metamodels = listOf(Meta.aaa, Meta.bbb, Meta.ccc, Meta.compositeKeyAddress)

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
