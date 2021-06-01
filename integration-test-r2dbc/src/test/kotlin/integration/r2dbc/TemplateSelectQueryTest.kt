package integration.r2dbc

import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcTemplateDsl
import org.komapper.r2dbc.dsl.query.Row

@ExtendWith(Env::class)
class TemplateSelectQueryTest(private val db: R2dbcDatabase) {

    private val asAddress: (Row) -> Address = { row ->
        Address(
            row.asInt("address_id")!!,
            row.asString("street")!!,
            row.asInt("version")!!
        )
    }

    // TODO index
    private val asAddressByIndex: (Row) -> Address = { row ->
        Address(
            row.asInt(0)!!,
            row.asString(1)!!,
            row.asInt(2)!!
        )
    }

    @Test
    fun test_columnLabel() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS"
            R2dbcTemplateDsl.from(sql).select(asAddress)
        }.toList()
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun test_index() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS"
            R2dbcTemplateDsl.from(sql).select(asAddressByIndex)
        }.toList()
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun condition_objectExpression() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where street = /*street*/'test'"
            R2dbcTemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val street = "STREET 10"
                }
            }.select(asAddress)
        }.toList()
        assertEquals(1, list.size)
        assertEquals(
            Address(
                10,
                "STREET 10",
                1
            ),
            list[0]
        )
    }

    @Test
    fun condition_dataClass() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where street = /*street*/'test'"
            R2dbcTemplateDsl.from(sql).where {
                data class Condition(val street: String)
                Condition("STREET 10")
            }.select(asAddress)
        }.toList()
        assertEquals(1, list.size)
        assertEquals(
            Address(
                10,
                "STREET 10",
                1
            ),
            list[0]
        )
    }

    @Test
    fun `in`() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where address_id in /*list*/(0)"
            R2dbcTemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val list = listOf(1, 2)
                }
            }.select(asAddress)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
        assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ),
            list[1]
        )
    }

    @Test
    fun in2() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where (address_id, street) in /*pairs*/(0, '')"
            R2dbcTemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val pairs = listOf(1 to "STREET 1", 2 to "STREET 2")
                }
            }.select(asAddress)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
        assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ),
            list[1]
        )
    }

    @Test
    fun in3() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where (address_id, street, version) in /*triples*/(0, '', 0)"
            R2dbcTemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val triples = listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1)
                    )
                }
            }.select(asAddress)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
        assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ),
            list[1]
        )
    }

    @Test
    fun escape() = inTransaction(db) {
        val list = db.runQuery {
            val sql =
                """
                select * from ADDRESS 
                where street like concat(/* street.escape() */'test', '%')
                order by address_id
                """.trimIndent()
            R2dbcTemplateDsl.from(sql).where {
                data class Condition(val street: String)
                Condition("STREET 1")
            }.select(asAddress)
        }.toList()
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun asPrefix() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where street like /*street.asPrefix()*/'test' order by address_id"
            R2dbcTemplateDsl.from(sql).where {
                data class Condition(val street: String)
                Condition("STREET 1")
            }.select(asAddress)
        }.toList()
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }
}
