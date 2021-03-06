package integration.r2dbc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.ScriptDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.r2dbc.R2dbcDatabase

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
            ScriptDsl.execute(script)
        }

        val value = db.runQuery {
            val sql = "select value from execute_table"
            TemplateDsl.from(sql).select { row ->
                row.asString("value")
            }.first()
        }
        Assertions.assertEquals("test", value)
    }
}
