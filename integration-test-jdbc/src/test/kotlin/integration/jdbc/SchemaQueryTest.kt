package integration.jdbc

import integration.aaa
import integration.bbb
import integration.ccc
import integration.compositeKeyAddress
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.SchemaDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: JdbcDatabase) {

    private val metamodels = listOf(Meta.aaa, Meta.bbb, Meta.ccc, Meta.compositeKeyAddress)

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
