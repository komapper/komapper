package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import integration.core.aaa
import integration.core.assignment
import integration.core.autoIncrementTable
import integration.core.bbb
import integration.core.belonging
import integration.core.ccc
import integration.core.compositeKey
import integration.core.ddd
import integration.core.sequenceTable
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dryRunQuery
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcSchemaTest(private val db: JdbcDatabase) {
    private val metamodels =
        listOf(
            Meta.aaa,
            Meta.bbb,
            Meta.ccc,
            Meta.ddd,
            Meta.compositeKey,
            Meta.autoIncrementTable,
            Meta.sequenceTable,
        )

    @Test
    fun create() {
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
    fun create_check() {
        db.runQuery {
            QueryDsl.create(metamodels)
        }
        // check existence
        db.runQuery {
            QueryDsl.from(Meta.aaa)
                .andThen(QueryDsl.from(Meta.bbb))
                .andThen(QueryDsl.from(Meta.ccc))
                .andThen(QueryDsl.from(Meta.ddd))
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
    fun drop() {
        db.runQuery {
            QueryDsl.drop(metamodels)
        }
        // tear down
        db.runQuery {
            QueryDsl.drop(metamodels)
        }
    }

    @Test
    fun virtualId() {
        val result = db.dryRunQuery {
            QueryDsl.create(Meta.belonging)
        }
        assertFalse(result.sql.contains("primary key"))
        db.runQuery {
            QueryDsl.create(Meta.belonging)
        }
        db.runQuery {
            QueryDsl.drop(Meta.belonging)
        }
    }

    @Test
    fun virtualEmbeddedId() {
        val result = db.dryRunQuery {
            QueryDsl.create(Meta.assignment)
        }
        assertFalse(result.sql.contains("primary key"))
        db.runQuery {
            QueryDsl.create(Meta.assignment)
        }
        db.runQuery {
            QueryDsl.drop(Meta.assignment)
        }
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun column_length_applied() {
        val result = db.dryRunQuery {
            QueryDsl.create(Meta.aaa)
        }
        assertTrue(result.sql.contains("varchar(1000)"), result.sql)
    }

    @Run(unless = [Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun column_length_not_applied() {
        val result = db.dryRunQuery {
            QueryDsl.create(Meta.ddd)
        }
        assertFalse(result.sql.contains("integer(1000)"), result.sql)
        assertTrue(result.sql.contains("integer"), result.sql)
    }
}
