package integration.jdbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.query.bind
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.get
import org.komapper.core.dsl.query.getNotNull
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcTemplateTest(private val db: JdbcDatabase) {

    data class NullAddress(val addressId: Int?, val street: String?, val version: String?)

    private val asAddress: (Row) -> Address = { row ->
        Address(
            row.getNotNull("address_id"),
            row.getNotNull("street"),
            row.getNotNull("version"),
        )
    }

    private val asAddressByIndex: (Row) -> Address = { row ->
        Address(
            row.getNotNull(0),
            row.getNotNull(1),
            row.getNotNull(2),
        )
    }

    @Test
    fun test_columnLabel() {
        val list = db.runQuery {
            val sql = "select * from address"
            QueryDsl.fromTemplate(sql).select(asAddress)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun test_columnLabel_null() {
        val list = db.runQuery {
            val sql = "select null as a, null as b, null as c from address"
            QueryDsl.fromTemplate(sql).select { row ->
                NullAddress(
                    row.get("a"),
                    row.get("b"),
                    row.get("c"),
                )
            }
        }
        assertEquals(15, list.size)
        list.forEach { address ->
            assertNull(address.addressId)
            assertNull(address.street)
            assertNull(address.version)
        }
    }

    @Test
    fun test_index() {
        val list = db.runQuery {
            val sql = "select * from address"
            QueryDsl.fromTemplate(sql).select(asAddressByIndex)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun test_index_null() {
        val list = db.runQuery {
            val sql = "select null, null, null from address"
            QueryDsl.fromTemplate(sql).select { row ->
                NullAddress(row.get(0), row.get(1), row.get(2))
            }
        }
        assertEquals(15, list.size)
        list.forEach { address ->
            assertNull(address.addressId)
            assertNull(address.street)
            assertNull(address.version)
        }
    }

    @Test
    fun sequence() {
        val list = db.runQuery {
            val sql = "select * from address"
            QueryDsl.fromTemplate(sql).select(asAddress).collect { it.toList() }
        }
        assertEquals(15, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1,
            ),
            list[0],
        )
    }

    @Test
    fun bind() {
        val list = db.runQuery {
            val sql = "select * from address where street = /*street*/'test'"
            QueryDsl.fromTemplate(sql)
                .bind("street", "STREET 10")
                .select(asAddress)
        }
        assertEquals(1, list.size)
        assertEquals(
            Address(
                10,
                "STREET 10",
                1,
            ),
            list[0],
        )
    }

    @Test
    fun bindNull() {
        val street: String? = null
        val list = db.runQuery {
            val sql = "select * from address where /*%if street != null*/ street = /*street*/'test' /*%end*/"
            QueryDsl.fromTemplate(sql)
                .bind("street", street)
                .select(asAddress)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun bind_unknownBindVariable() {
        val ex = assertFailsWith<Exception> {
            db.runQuery {
                val sql = "select * from address where street = /*street*/'test'"
                QueryDsl.fromTemplate(sql)
                    .bind("unknown", "STREET 10")
                    .select(asAddress)
            }
            Unit
        }
        val message = ex.message!!
        assertEquals("The expression evaluation was failed at <select * from address where street = /*street*/'test'>:1:47.", message)
        val causeMessage = ex.cause!!.message
        assertEquals("The variable \"street\" is not found. Make sure the variable name is correct. <street>:6", causeMessage)
    }

    @Test
    fun `in`() {
        val list = db.runQuery {
            val sql = "select * from address where address_id in /*list*/(0)"
            QueryDsl.fromTemplate(sql)
                .bind("list", listOf(1, 2))
                .select(asAddress)
        }
        assertEquals(2, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1,
            ),
            list[0],
        )
        assertEquals(
            Address(
                2,
                "STREET 2",
                1,
            ),
            list[1],
        )
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in2() {
        val list = db.runQuery {
            val sql = "select * from address where (address_id, street) in /*pairs*/(0, '')"
            QueryDsl.fromTemplate(sql)
                .bind("pairs", listOf(1 to "STREET 1", 2 to "STREET 2"))
                .select(asAddress)
        }
        assertEquals(2, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1,
            ),
            list[0],
        )
        assertEquals(
            Address(
                2,
                "STREET 2",
                1,
            ),
            list[1],
        )
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in3() {
        val list = db.runQuery {
            val sql = "select * from address where (address_id, street, version) in /*triples*/(0, '', 0)"
            QueryDsl.fromTemplate(sql)
                .bind(
                    "triples",
                    listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1),
                    ),
                ).select(asAddress)
        }
        assertEquals(2, list.size)
        assertEquals(
            Address(
                1,
                "STREET 1",
                1,
            ),
            list[0],
        )
        assertEquals(
            Address(
                2,
                "STREET 2",
                1,
            ),
            list[1],
        )
    }

    @Test
    fun escape() {
        val list = db.runQuery {
            val sql =
                """
                select * from address 
                where street like concat(/* street.escape() */'test', '%')
                order by address_id
                """.trimIndent()
            QueryDsl.fromTemplate(sql).bind(
                "street",
                "STREET 1",
            ).select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun asPrefix() {
        val list = db.runQuery {
            val sql = "select * from address where street like /*street.asPrefix()*/'test' order by address_id"
            QueryDsl.fromTemplate(sql).bind("street", "STREET 1").select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun execute() {
        val count = db.runQuery {
            val sql = "update address set street = /*street*/'' where address_id = /*id*/0"
            QueryDsl.executeTemplate(sql)
                .bind("id", 15)
                .bind("street", "NY street")
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
                1,
            ),
            address,
        )
    }
}
