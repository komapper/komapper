package integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.Database
import org.komapper.jdbc.dsl.ScriptDsl
import org.komapper.jdbc.dsl.TemplateDsl
import org.komapper.jdbc.dsl.runQuery

@ExtendWith(Env::class)
internal class ScriptExecuteQueryTest(private val db: Database) {

    @Test
    fun test() {
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
