package integration.jdbc

import integration.Aaa
import integration.Bbb
import integration.Ccc
import integration.CompositeKeyAddress
import integration.meta
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SchemaDsl
import org.komapper.jdbc.JdbcDatabase

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: JdbcDatabase) {

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
