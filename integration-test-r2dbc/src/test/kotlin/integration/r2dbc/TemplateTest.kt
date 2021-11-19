package integration.r2dbc

import integration.Address
import integration.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class TemplateTest(private val db: R2dbcDatabase) {

    private val asAddress: (Row) -> Address = { row ->
        Address(
            row.asInt("address_id")!!,
            row.asString("street")!!,
            row.asInt("version")!!
        )
    }

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
            TemplateDsl.from(sql).select(asAddress)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun test_index() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS"
            TemplateDsl.from(sql).select(asAddressByIndex)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun condition_objectExpression() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where street = /*street*/'test'"
            TemplateDsl.from(sql).bind(
                object {
                    @Suppress("unused")
                    val street = "STREET 10"
                }
            ).select(asAddress)
        }
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
            data class Condition(val street: String)

            val sql = "select * from ADDRESS where street = /*street*/'test'"
            TemplateDsl.from(sql).bind(Condition("STREET 10")).select(asAddress)
        }
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
            TemplateDsl.from(sql).bind(
                object {
                    @Suppress("unused")
                    val list = listOf(1, 2)
                }
            ).select(asAddress)
        }
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
            TemplateDsl.from(sql).bind(
                object {
                    @Suppress("unused")
                    val pairs = listOf(1 to "STREET 1", 2 to "STREET 2")
                }
            ).select(asAddress)
        }
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
            TemplateDsl.from(sql).bind(
                object {
                    @Suppress("unused")
                    val triples = listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1)
                    )
                }
            ).select(asAddress)
        }
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
            data class Condition(val street: String)

            val sql =
                """
                select * from ADDRESS 
                where street like concat(/* street.escape() */'test', '%')
                order by address_id
                """.trimIndent()
            TemplateDsl.from(sql).bind(
                Condition("STREET 1")
            ).select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun asPrefix() = inTransaction(db) {
        val list = db.runQuery {
            data class Condition(val street: String)

            val sql = "select * from ADDRESS where street like /*street.asPrefix()*/'test' order by address_id"
            TemplateDsl.from(sql).bind(Condition("STREET 1")).select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun execute() = inTransaction(db) {
        val count = db.runQuery {
            data class Condition(val id: Int, val street: String)

            val sql = "update ADDRESS set street = /*street*/'' where address_id = /*id*/0"
            TemplateDsl.execute(sql).bind(Condition(15, "NY street"))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 15
            }.first()
        }
        assertEquals(
            Address(
                15,
                "NY street",
                1
            ),
            address
        )
    }
}
