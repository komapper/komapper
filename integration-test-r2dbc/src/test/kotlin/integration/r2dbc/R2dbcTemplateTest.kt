package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.query.bind
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcTemplateTest(private val db: R2dbcDatabase) {

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
            val sql = "select * from address"
            QueryDsl.fromTemplate(sql).select(asAddress)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun test_index() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from address"
            QueryDsl.fromTemplate(sql).select(asAddressByIndex)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun bind() = inTransaction(db) {
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
                1
            ),
            list[0]
        )
    }

    @Test
    fun bindNull() = inTransaction(db) {
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
    fun `in`() = inTransaction(db) {
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

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in2() = inTransaction(db) {
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

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in3() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from address where (address_id, street, version) in /*triples*/(0, '', 0)"
            QueryDsl.fromTemplate(sql)
                .bind(
                    "triples",
                    listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1)
                    )
                )
                .select(asAddress)
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
            val sql =
                """
                select * from address 
                where street like concat(/* street.escape() */'test', '%')
                order by address_id
                """.trimIndent()
            QueryDsl.fromTemplate(sql)
                .bind("street", "STREET 1")
                .select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun asPrefix() = inTransaction(db) {
        val list = db.runQuery {
            val sql = "select * from address where street like /*street.asPrefix()*/'test' order by address_id"
            QueryDsl.fromTemplate(sql)
                .bind("street", "STREET 1")
                .select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun execute() = inTransaction(db) {
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
                1
            ),
            address
        )
    }
}
