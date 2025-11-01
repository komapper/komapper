package org.komapper.exposed.r2dbc

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class R2dbcTemplateTest {
    private val db = R2dbcDatabase.connect("r2dbc:h2:mem:///test;DB_CLOSE_DELAY=-1")

    enum class Status {
        NEW,
        IN_PROGRESS,
        DONE,
    }

    object Tasks : IntIdTable("tasks") {
        val title = varchar("title", 128)
        val description = varchar("description", 128)
        val statusName = enumerationByName<Status>("status_name", 10, Status::class)
        val statusOrdinal = enumeration("status_ordinal", Status::class)
    }

    @BeforeTest
    fun before() {
        runBlocking {
            suspendTransaction(db) {
                addLogger(StdOutSqlLogger)
                SchemaUtils.drop(Tasks)
                SchemaUtils.create(Tasks)

                Tasks.insert {
                    it[title] = "Learn Exposed DAO"
                    it[description] = "Follow the DAO tutorial"
                    it[statusName] = Status.NEW
                    it[statusOrdinal] = Status.NEW
                }

                Tasks.insert {
                    it[title] = "Read The Hobbit"
                    it[description] = "Read chapter one"
                    it[statusName] = Status.DONE
                    it[statusOrdinal] = Status.DONE
                }
            }
        }
    }

    @Test
    fun bind_string() = runBlocking {
        suspendTransaction(db) {
            addLogger(StdOutSqlLogger)

            val result = r2dbcTemplate {
                val title by arg("Read The Hobbit", Tasks.title)
                build(
                    """
                    select id from tasks where title = /* $title */''                
                    """.trimIndent()
                )
            }.execute {
                it.get("id", Integer::class.java)
            }?.toList()?.map { it?.toInt() }

            assertEquals(1, result!!.size)
            assertEquals(2, result.first())
        }
    }
}
