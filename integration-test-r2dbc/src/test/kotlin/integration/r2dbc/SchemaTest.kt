package integration.r2dbc

import integration.core.aaa
import integration.core.autoIncrementTable
import integration.core.bbb
import integration.core.ccc
import integration.core.compositeKey
import integration.core.sequenceTable
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test

@ExtendWith(Env::class)
class SchemaTest(private val db: R2dbcDatabase) {

    private val metamodels =
        listOf(
            Meta.aaa,
            Meta.bbb,
            Meta.ccc,
            Meta.compositeKey,
            Meta.autoIncrementTable,
            Meta.sequenceTable
        )

    @Test
    fun create() = inTransaction(db) {
        db.runQuery {
            QueryDsl.create(metamodels)
        }
        db.runQuery {
            QueryDsl.create(metamodels)
        }
        // tear down
        db.runQuery {
            QueryDsl.drop(metamodels)
        }
    }

    @Test
    fun create_check() = inTransaction(db) {
        db.runQuery {
            QueryDsl.create(metamodels)
        }
        // check existence
        db.runQuery {
            QueryDsl.from(Meta.aaa)
                .andThen(QueryDsl.from(Meta.bbb))
                .andThen(QueryDsl.from(Meta.ccc))
                .andThen(QueryDsl.from(Meta.compositeKey))
                .andThen(QueryDsl.from(Meta.autoIncrementTable))
                .andThen(QueryDsl.from(Meta.sequenceTable))
        }
        // tear down
        db.runQuery {
            QueryDsl.drop(metamodels)
        }
    }

    @Test
    fun drop() = inTransaction(db) {
        db.runQuery {
            QueryDsl.drop(metamodels)
        }
        // tear down
        db.runQuery {
            QueryDsl.drop(metamodels)
        }
    }
}
