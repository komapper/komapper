package org.komapper.jdbc.h2

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database

@ExtendWith(Env::class)
class QuotesTest(private val db: Database) {

    /*
    @Test
    fun test() {
        val messages = mutableListOf<String>()
        val logger = object : StdoutLogger() {
            override fun logStmt(stmt: Stmt) {
                val message = stmt.log!!
                println(message)
                messages.add(message)
            }
        }
        val myConfig = object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val entityMetaResolver = db.config.entityMetaResolver
            override val logger: Logger = logger
        }

        val db = Db(myConfig)
        db.insert(Quotes(id = 0, value = "aaa"))
        Assertions.assertEquals(
            listOf(
                "call next value for \"SEQUENCE_STRATEGY_ID\"",
                "insert into \"SEQUENCE_STRATEGY\" (\"ID\", \"VALUE\") values (1, 'aaa')"
            ), messages
        )

        messages.clear()
        db.select<Quotes>().first()
        Assertions.assertEquals(
            listOf(
                "select t0_.\"ID\", t0_.\"VALUE\" from \"SEQUENCE_STRATEGY\" t0_"
            ), messages
        )
    }
     */
}
