package integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SchemaDsl
import org.komapper.jdbc.Database

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: Database) {

    private val metamodels = listOf(Aaa.meta, Bbb.meta, Ccc.meta, CompositeKeyAddress.meta)

    @Test
    fun create() {
        db.runQuery {
            SchemaDsl.create(metamodels)
        }
    }

    @Test
    fun drop() {
        db.runQuery {
            SchemaDsl.create(metamodels)
        }
        db.runQuery {
            SchemaDsl.drop(metamodels)
        }
    }
}
