package org.komapper.exposed

import org.jetbrains.exposed.v1.core.EnumerationNameColumnType
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class JdbcTemplateTest {
    private val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

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

    class Task(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Task>(Tasks)

        var title by Tasks.title
        var description by Tasks.description
        var statusName by Tasks.statusName
        var statusOrdinal by Tasks.statusOrdinal

        override fun toString(): String {
            return "Task(id=$id, title=$title, statusName=$statusName), statusOrdinal=$statusOrdinal"
        }
    }

    @BeforeTest
    fun before() {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(Tasks)
            SchemaUtils.create(Tasks)

            Task.new {
                title = "Learn Exposed DAO"
                description = "Follow the DAO tutorial"
                statusName = Status.NEW
                statusOrdinal = statusName
            }

            Task.new {
                title = "Read The Hobbit"
                description = "Read chapter one"
                statusName = Status.DONE
                statusOrdinal = statusName
            }
        }
    }

    @Test
    fun bind_entityID() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val id by arg(EntityID(1, Tasks), Tasks.id)
            build(
                """
                select title from tasks where id = /* $id */0                
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<String>()
            while (it.next()) {
                list += it.getString("title")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals("Learn Exposed DAO", result.first())
    }

    @Test
    fun bind_string() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val title by arg("Read The Hobbit", Tasks.title)
            build(
                """
                select id from tasks where title = /* $title */''                
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<Int>()
            while (it.next()) {
                list += it.getInt("id")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals(2, result.first())
    }

    @Test
    fun bind_varCharColumnType() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val title by arg("Read The Hobbit", VarCharColumnType())
            build(
                """
                select id from tasks where title = /* $title */''                
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<Int>()
            while (it.next()) {
                list += it.getInt("id")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals(2, result.first())
    }

    @Test
    fun bind_expression() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val title by arg("Hobbit", Tasks.title)
            build(
                """
                select id from tasks where title like /* $title.asSuffix() */''                
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<Int>()
            while (it.next()) {
                list += it.getInt("id")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals(2, result.first())
    }

    @Test
    fun bind_enum_name() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val statusName by arg(Status.NEW, Tasks.statusName)
            build(
                """
                select title from tasks where status_name = /* $statusName */''
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<String>()
            while (it.next()) {
                list += it.getString("title")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals("Learn Exposed DAO", result.first())
    }

    @Test
    fun bind_enum_ordinal() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val statusOrdinal by arg(Status.NEW, Tasks.statusOrdinal)
            build(
                """
                select title from tasks where status_ordinal = /* $statusOrdinal */''
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<String>()
            while (it.next()) {
                list += it.getString("title")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals("Learn Exposed DAO", result.first())
    }

    @Test
    fun bind_multiple_columns() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val statusName by arg(Status.NEW, Tasks.statusName)
            val title by arg("Learn Exposed DAO", Tasks.title)
            build(
                """
                select
                    title 
                from
                    tasks
                where
                    title = /* $title */''
                    and
                    status_name = /* $statusName */''              
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<String>()
            while (it.next()) {
                list += it.getString("title")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals("Learn Exposed DAO", result.first())
    }

    @Test
    fun bind_multiple_columnTypes() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val statusName by arg(Status.NEW, EnumerationNameColumnType<Status>(Status::class, 10))
            val title by arg("Learn Exposed DAO", VarCharColumnType())
            build(
                """
                select
                    title 
                from
                    tasks
                where
                    title = /* $title */''
                    and
                    status_name = /* $statusName */''              
                """
            )
        }.execute {
            val list = mutableListOf<String>()
            while (it.next()) {
                list += it.getString("title")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals("Learn Exposed DAO", result.first())
    }

    @Test
    fun if_directive_true() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val id by arg(EntityID(1, Tasks), Tasks.id)
            build(
                """
                select title from tasks where 
                /*% if $id != null */id = /* $id */0 /*% end */
                order by id                
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<String>()
            while (it.next()) {
                list += it.getString("title")
            }
            list
        }

        assertEquals(1, result!!.size)
        assertEquals("Learn Exposed DAO", result.first())
    }

    @Test
    fun if_directive_false() = transaction(db) {
        addLogger(StdOutSqlLogger)

        val result = jdbcTemplate {
            val title by arg(null, Tasks.title)
            build(
                """
                select title from tasks where 
                /*% if $title != null */title = /* $title */'' /*% end */
                order by id                
                """.trimIndent()
            )
        }.execute {
            val list = mutableListOf<String>()
            while (it.next()) {
                list += it.getString("title")
            }
            list
        }
        assertEquals(2, result!!.size)
        assertEquals(listOf("Learn Exposed DAO", "Read The Hobbit"), result)
    }
}
