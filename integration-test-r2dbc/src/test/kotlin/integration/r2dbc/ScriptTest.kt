package integration.r2dbc

import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.ScriptDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@Tags(
    value = [
        Tag("lowPriority"),
        Tag("script")
    ]
)
@ExtendWith(Env::class)
internal class ScriptTest(private val db: R2dbcDatabase) {

    @Run(unless = [Dbms.MARIADB, Dbms.MYSQL])
    @Test
    fun test_double_quote() = inTransaction(db) {
        db.runQuery {
            val script = """
            create table execute_table("value" varchar(20));
            insert into execute_table("value") values('test');
            """
            ScriptDsl.execute(script)
        }

        val value = db.runQuery {
            val sql = """select "value" from execute_table"""
            TemplateDsl.from(sql).select { row ->
                row.asString("value")
            }.first()
        }
        assertEquals("test", value)

        db.runQuery {
            val script = """
            drop table execute_table;
            """
            ScriptDsl.execute(script)
        }
    }

    @Run(onlyIf = [Dbms.MARIADB, Dbms.MYSQL])
    @Test
    fun test_back_quote() = inTransaction(db) {
        db.runQuery {
            val script = """
            create table execute_table(`value` varchar(20));
            insert into execute_table(`value`) values('test');
            """
            ScriptDsl.execute(script)
        }

        val value = db.runQuery {
            val sql = "select `value` from execute_table"
            TemplateDsl.from(sql).select { row ->
                row.asString("value")
            }.first()
        }
        assertEquals("test", value)

        db.runQuery {
            val script = """
            drop table execute_table;
            """
            ScriptDsl.execute(script)
        }
    }
}
