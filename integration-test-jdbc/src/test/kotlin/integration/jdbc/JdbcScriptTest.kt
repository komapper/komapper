package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.string
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
internal class JdbcScriptTest(private val db: JdbcDatabase) {

    @Run(unless = [Dbms.MARIADB, Dbms.MYSQL])
    @Test
    fun test_double_quote() {
        db.runQuery {
            val script = """
            create table execute_table("value" varchar(20));
            insert into execute_table("value") values('test');
            """
            QueryDsl.executeScript(script)
        }

        val value = db.runQuery {
            val sql = """select "value" from execute_table"""
            QueryDsl.fromTemplate(sql).select { row ->
                row.string("value")
            }.first()
        }
        assertEquals("test", value)

        db.runQuery {
            val script = """
            drop table execute_table;
            """
            QueryDsl.executeScript(script)
        }
    }

    @Run(onlyIf = [Dbms.MARIADB, Dbms.MYSQL])
    @Test
    fun test_back_quote() {
        db.runQuery {
            val script = """
            create table execute_table(`value` varchar(20));
            insert into execute_table(`value`) values('test');
            """
            QueryDsl.executeScript(script)
        }

        val value = db.runQuery {
            val sql = "select `value` from execute_table"
            QueryDsl.fromTemplate(sql).select { row ->
                row.string("value")
            }.first()
        }
        assertEquals("test", value)

        db.runQuery {
            val script = """
            drop table execute_table;
            """
            QueryDsl.executeScript(script)
        }
    }
}
