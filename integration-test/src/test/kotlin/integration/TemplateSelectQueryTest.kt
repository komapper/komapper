package integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.TemplateQuery
import org.komapper.core.dsl.execute
import org.komapper.core.dsl.query.Row

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
        val list = db.execute {
            val sql = "select * from address"
            TemplateQuery.from(sql).select(asAddress)
        }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(
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
        val list = db.execute {
            val sql = "select * from address"
            TemplateQuery.from(sql).select(asAddress).collect { it.toList() }
        }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(
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
        val list = db.execute {
            val sql = "select * from address where street = /*street*/'test'"
            TemplateQuery.from(sql).where {
                object {
                    @Suppress("unused")
                    val street = "STREET 10"
                }
            }.select(asAddress)
        }
        Assertions.assertEquals(1, list.size)
        Assertions.assertEquals(
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
        val list = db.execute {
            val sql = "select * from address where street = /*street*/'test'"
            TemplateQuery.from(sql).where {
                data class Condition(val street: String)
                Condition("STREET 10")
            }.select(asAddress)
        }
        Assertions.assertEquals(1, list.size)
        Assertions.assertEquals(
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
        val list = db.execute {
            val sql = "select * from address where address_id in /*list*/(0)"
            TemplateQuery.from(sql).where {
                object {
                    @Suppress("unused")
                    val list = listOf(1, 2)
                }
            }.select(asAddress)
        }
        Assertions.assertEquals(2, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
        Assertions.assertEquals(
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
        val list = db.execute {
            val sql = "select * from address where (address_id, street) in /*pairs*/(0, '')"
            TemplateQuery.from(sql).where {
                object {
                    @Suppress("unused")
                    val pairs = listOf(1 to "STREET 1", 2 to "STREET 2")
                }
            }.select(asAddress)
        }
        Assertions.assertEquals(2, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
        Assertions.assertEquals(
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
        val list = db.execute {
            val sql = "select * from address where (address_id, street, version) in /*triples*/(0, '', 0)"
            TemplateQuery.from(sql).where {
                object {
                    @Suppress("unused")
                    val triples = listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1)
                    )
                }
            }.select(asAddress)
        }
        Assertions.assertEquals(2, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ),
            list[0]
        )
        Assertions.assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ),
            list[1]
        )
    }
}
