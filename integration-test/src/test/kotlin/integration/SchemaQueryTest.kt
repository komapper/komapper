package integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SchemaQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: Database) {

    private val metamodels = listOf(Aaa.alias, Bbb.alias, Ccc.alias)

    @Test
    fun create() {
        db.execute {
            SchemaQuery.create(metamodels)
        }
        SchemaQuery.create(metamodels).dryRun()
    }

    @Test
    fun drop() {
        db.execute {
            SchemaQuery.create(metamodels)
        }
        db.execute {
            SchemaQuery.drop(metamodels)
        }
    }

    @Test
    fun dropAll() {
        db.execute {
            SchemaQuery.create(metamodels)
        }
        db.execute {
            SchemaQuery.dropAll()
        }
    }
}
