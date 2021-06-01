package integration.r2dbc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcScriptDsl
import org.komapper.r2dbc.dsl.R2dbcTemplateDsl

@ExtendWith(Env::class)
internal class ScriptExecuteQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        db.runQuery {
            val script = """
            drop table if exists execute_table;
            create table execute_table(value varchar(20));
            insert into execute_table(value) values('test');
            """
            R2dbcScriptDsl.execute(script)
        }

        val value = db.runQuery {
            val sql = "select value from execute_table"
            R2dbcTemplateDsl.from(sql).select { row ->
                row.asString("value")
            }.first()
        }
        Assertions.assertEquals("test", value)
    }
}
