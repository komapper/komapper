package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.TemplateDsl
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class TemplateSelectQueryTest(private val db: Database) {

    private val asAddress: Row.() -> Address = {
        Address(
            asInt("address_id")!!,
            asString("street")!!,
            asInt("version")!!
        )
    }

    @Test
    fun test() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS"
            TemplateDsl.from(sql).select(asAddress)
        }
        assertEquals(15, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
    }

    @Test
    fun sequence() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS"
            TemplateDsl.from(sql).select(asAddress).collect { it.toList() }
        }
        assertEquals(15, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
    }

    @Test
    fun condition_objectExpression() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where street = /*street*/'test'"
            TemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val street = "STREET 10"
                }
            }.select(asAddress)
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
    fun condition_dataClass() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where street = /*street*/'test'"
            TemplateDsl.from(sql).where {
                data class Condition(val street: String)
                Condition("STREET 10")
            }.select(asAddress)
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
    fun `in`() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where address_id in /*list*/(0)"
            TemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val list = listOf(1, 2)
                }
            }.select(asAddress)
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
    fun in2() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where (address_id, street) in /*pairs*/(0, '')"
            TemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val pairs = listOf(1 to "STREET 1", 2 to "STREET 2")
                }
            }.select(asAddress)
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
    fun in3() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where (address_id, street, version) in /*triples*/(0, '', 0)"
            TemplateDsl.from(sql).where {
                object {
                    @Suppress("unused")
                    val triples = listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1)
                    )
                }
            }.select(asAddress)
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
    fun escape() {
        val list = db.runQuery {
            val sql =
                """
                select * from ADDRESS 
                where street like concat(/* street.escape() */'test', '%')
                order by address_id
                """.trimIndent()
            TemplateDsl.from(sql).where {
                data class Condition(val street: String)
                Condition("STREET 1")
            }.select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun asPrefix() {
        val list = db.runQuery {
            val sql = "select * from ADDRESS where street like /*street.asPrefix()*/'test' order by address_id"
            TemplateDsl.from(sql).where {
                data class Condition(val street: String)
                Condition("STREET 1")
            }.select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }
}
