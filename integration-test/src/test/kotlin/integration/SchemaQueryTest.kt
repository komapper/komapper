package integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SchemaQuery
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: Database) {

    private val metamodels = listOf(Aaa.alias, Bbb.alias, Ccc.alias, CompositeKeyAddress.alias)

    @Test
    fun create() {
        db.runQuery {
            SchemaQuery.create(metamodels)
        }
    }

    @Test
    fun drop() {
        db.runQuery {
            SchemaQuery.create(metamodels)
        }
        db.runQuery {
            SchemaQuery.drop(metamodels)
        }
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun dropAll() {
        db.runQuery {
            SchemaQuery.create(metamodels)
        }
        db.runQuery {
            SchemaQuery.dropAll()
        }
    }
}
