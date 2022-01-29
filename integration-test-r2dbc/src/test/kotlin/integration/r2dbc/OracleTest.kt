package integration.r2dbc

import integration.setting.Dbms
import integration.setting.Run
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class OracleTest(val db: R2dbcDatabase) {

    @Tag("reproduction1")
    @Test
    fun reproduction1(): Unit = runBlockingWithTimeout {
        val config = db.config as DefaultR2dbcDatabaseConfig
        config.connectionFactory.create().awaitFirst().let { con ->
            con.beginTransaction().awaitFirstOrNull()
            val statement = con.createStatement("select 'a' from address")
            val result = statement.execute().awaitFirst()
            val value = result.map { row, _ -> row.get(0) }.awaitFirst()
            println(value)
            con.rollbackTransaction()
        }
        config.connectionFactory.create().awaitFirst().let { con ->
            con.beginTransaction().awaitFirstOrNull()
            val statement = con.createStatement("select 'b' from address")
            val result = statement.execute().awaitFirst()
            val value = result.map { row, _ -> row.get(0) }.awaitFirst()
            println(value)
            con.rollbackTransaction()
        }
    }

    @Run(onlyIf = [Dbms.ORACLE])
    @Tag("reproduction2")
    @Test
    fun reproduction2(): Unit = runBlockingWithTimeout {
        val config = db.config as DefaultR2dbcDatabaseConfig
        config.connectionFactory.create().awaitFirst().let { con ->
            con.beginTransaction().awaitFirstOrNull()
            val statement = con.createStatement("select 'a' from dual")
            val result = statement.execute().awaitFirst()
            val value = result.map { row, _ -> row.get(0) }.awaitFirst()
            println(value)
            con.rollbackTransaction().awaitFirstOrNull()
        }
        config.connectionFactory.create().awaitFirst().let { con ->
            con.beginTransaction().awaitFirstOrNull()
            val statement = con.createStatement("select 'b' from dual")
            val result = statement.execute().awaitFirst()
            val value = result.map { row, _ -> row.get(0) }.awaitFirst()
            println(value)
            con.rollbackTransaction().awaitFirstOrNull()
        }
    }

}