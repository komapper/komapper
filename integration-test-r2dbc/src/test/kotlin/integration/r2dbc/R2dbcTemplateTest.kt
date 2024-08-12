package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.selectAsAddress
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.ProjectionType
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.query.bind
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.get
import org.komapper.core.dsl.query.getNotNull
import org.komapper.core.dsl.query.single
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(R2dbcEnv::class)
class R2dbcTemplateTest(private val db: R2dbcDatabase) {

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
    fun test_columnLabel(info: TestInfo) = inTransaction(db, info) {
        val list = db.runQuery {
            val sql = "select * from address"
            QueryDsl.fromTemplate(sql).select(asAddress)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun test_index(info: TestInfo) = inTransaction(db, info) {
        val list = db.runQuery {
            val sql = "select * from address"
            QueryDsl.fromTemplate(sql).select(asAddressByIndex)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun bind(info: TestInfo) = inTransaction(db, info) {
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
    fun bindNull(info: TestInfo) = inTransaction(db, info) {
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
    fun bind_unknownBindVariable(info: TestInfo) = inTransaction(db, info) {
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
        assertEquals("The template variable \"street\" is not bound to a value. Make sure the variable name is correct. <street>:6", causeMessage)
    }

    @Test
    fun `in`(info: TestInfo) = inTransaction(db, info) {
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
    fun in2(info: TestInfo) = inTransaction(db, info) {
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
    fun in3(info: TestInfo) = inTransaction(db, info) {
        val list = db.runQuery {
            val sql = "select * from address where (address_id, street, version) in /*triples*/(0, '', 0)"
            QueryDsl.fromTemplate(sql)
                .bind(
                    "triples",
                    listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1),
                    ),
                )
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

    @Test
    fun escape(info: TestInfo) = inTransaction(db, info) {
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
    fun asPrefix(info: TestInfo) = inTransaction(db, info) {
        val list = db.runQuery {
            val sql = "select * from address where street like /*street.asPrefix()*/'test' order by address_id"
            QueryDsl.fromTemplate(sql)
                .bind("street", "STREET 1")
                .select(asAddress)
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun execute(info: TestInfo) = inTransaction(db, info) {
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

    @Test
    fun selectAsEntity_byIndex(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val list = db.runQuery {
            val sql = "select address_id, street, version from address order by address_id"
            QueryDsl.fromTemplate(sql).selectAsEntity(a)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun selectAsEntity_byName(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val list = db.runQuery {
            val sql = "select street, version, address_id from address order by address_id"
            QueryDsl.fromTemplate(sql).selectAsEntity(a, ProjectionType.NAME)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun selectAsAddress_byIndex(info: TestInfo) = inTransaction(db, info) {
        val list = db.runQuery {
            val sql = "select address_id, street, version from address order by address_id"
            QueryDsl.fromTemplate(sql).selectAsAddress()
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun selectAsAddress_byName(info: TestInfo) = inTransaction(db, info) {
        val list = db.runQuery {
            val sql = "select street, version, address_id from address order by address_id"
            QueryDsl.fromTemplate(sql).selectAsAddress(ProjectionType.NAME)
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    @Run(unless = [Dbms.H2, Dbms.MYSQL, Dbms.MYSQL_5, Dbms.SQLSERVER, Dbms.ORACLE])
    fun insertReturning(info: TestInfo) = inTransaction(db, info) {
        val address = db.runQuery {
            val sql = """
                insert into address
                    (address_id, street, version)
                values
                    (/*id*/0, /*street*/'', /*version*/0)
                returning address_id, street, version
            """.trimIndent()
            QueryDsl.executeTemplate(sql)
                .returning()
                .bind("id", 16)
                .bind("street", "NY street")
                .bind("version", 1)
                .select(asAddress)
                .single()
        }
        assertEquals(
            Address(
                16,
                "NY street",
                1,
            ),
            address,
        )
    }
}
