package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.ScriptQuery
import org.komapper.core.dsl.TemplateQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
internal class ScriptExecuteQueryTest(private val db: Database) {

    @Test
    fun test() {
        db.execute {
            val script = """
            create table execute_table(value varchar(20));
            insert into execute_table(value) values('test');
            """
            ScriptQuery.execute(script)
        }

        val value = db.execute {
            val sql = "select value from execute_table"
            TemplateQuery.select(sql) {
                asString("value")
            }.first()
        }
        Assertions.assertEquals("test", value)
    }
}
