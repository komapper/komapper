package integration.jdbc

import integration.core.Address
import integration.core.AddressDto
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.enumPropertyData
import integration.core.enumclass.Color
import integration.core.selectAsAddress
import integration.core.selectAsAddressDto
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.annotation.KomapperCommand
import org.komapper.core.Exec
import org.komapper.core.Many
import org.komapper.core.One
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.ProjectionType
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.getNotNull
import org.komapper.core.dsl.query.single
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class JdbcCommandTest(private val db: JdbcDatabase) {

    private val asAddress: (Row) -> Address = { row ->
        Address(
            row.getNotNull("address_id"),
            row.getNotNull("street"),
            row.getNotNull("version"),
        )
    }

    @KomapperCommand(
        """
        select * from address where /*%if street != null*/ street = /*street*/'test' /*%end*/
        """,
    )
    data class Bind(val street: String, val asAddress: (Row) -> Address) : One<Address> {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress).single()
    }

    @Test
    fun bind() {
        val address = db.runQuery {
            QueryDsl.fromCommand(Bind("STREET 10", asAddress))
        }
        assertEquals(
            Address(
                10,
                "STREET 10",
                1,
            ),
            address,
        )
    }

    @KomapperCommand(
        """
        select * from address where /*%if street != null*/ street = /*street*/'test' /*%end*/
        """,
    )
    data class BindNull(val street: String?, val asAddress: (Row) -> Address) : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress)
    }

    @Test
    fun bindNull() {
        val list = db.runQuery {
            QueryDsl.fromCommand(BindNull(null, asAddress))
        }
        assertEquals(15, list.size)
    }

    @KomapperCommand(
        """
        select * from address where address_id in /*list*/(0)
        """,
    )
    data class In(val list: List<Int>, val asAddress: (Row) -> Address) : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress)
    }

    @Test
    fun `in`() {
        val list = db.runQuery {
            QueryDsl.fromCommand(In(listOf(1, 2), asAddress))
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

    @KomapperCommand(
        """
        select * from address where (address_id, street) in /*pairs*/(0, '')
        """,
    )
    data class In2(val pairs: List<Pair<Int, String>>, val asAddress: (Row) -> Address) : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in2() {
        val list = db.runQuery {
            QueryDsl.fromCommand(In2(listOf(1 to "STREET 1", 2 to "STREET 2"), asAddress))
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

    @KomapperCommand(
        """
        select * from address where (address_id, street, version) in /*triples*/(0, '', 0)
        """,
    )
    data class In3(val triples: List<Triple<Int, String, Int>>, val asAddress: (Row) -> Address) : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in3() {
        val list = db.runQuery {
            QueryDsl.fromCommand(
                In3(
                    listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1),
                    ),
                    asAddress,
                ),
            )
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

    // TODO
    @KomapperCommand(
        """
        select * from address 
        where street like concat(/* street.escape() */'test', '%')
        order by address_id
        """,
        disableValidation = true,
    )
    data class Escape(val street: String, val asAddress: (Row) -> Address) : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress)
    }

    @Test
    fun escape() {
        val list = db.runQuery {
            QueryDsl.fromCommand(Escape("STREET 1", asAddress))
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    // TODO
    @KomapperCommand(
        """
        select * from address 
        where street like concat(/* street.escape() */'test', '%')
        order by address_id
        """,
        disableValidation = true,
    )
    data class AsPrefix(val street: String, val asAddress: (Row) -> Address) : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress)
    }

    @Test
    fun asPrefix() {
        val list = db.runQuery {
            QueryDsl.fromCommand(AsPrefix("STREET 1", asAddress))
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @KomapperCommand(
        """
        update address set street = /*street*/'' where address_id = /*id*/0
        """,
    )
    data class Execute(val id: Int, val street: String) : Exec

    @Test
    fun execute() {
        val count = db.runQuery {
            QueryDsl.executeCommand(Execute(15, "NY street"))
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

    @KomapperCommand(
        """
        select address_id, street, version from address order by address_id
        """,
    )
    class SelectAsEntityByIndex : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = selectAsEntity(Meta.address)
    }

    @Test
    fun selectAsEntity_byIndex() {
        val list = db.runQuery {
            QueryDsl.fromCommand(SelectAsEntityByIndex())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select street, version, address_id from address order by address_id
        """,
    )
    class SelectAsEntityByName : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = selectAsEntity(Meta.address, ProjectionType.NAME)
    }

    @Test
    fun selectAsEntity_byName() {
        val list = db.runQuery {
            QueryDsl.fromCommand(SelectAsEntityByName())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select address_id, street, version from address order by address_id
        """,
    )
    class SelectAsAddressByIndex : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = selectAsAddress()
    }

    @Test
    fun selectAsAddress_byIndex() {
        val list = db.runQuery {
            QueryDsl.fromCommand(SelectAsAddressByIndex())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select street, version, address_id from address order by address_id
        """,
    )
    class SelectAsAddressByName : Many<Address> {
        override fun TemplateSelectQueryBuilder.execute() = selectAsAddress(ProjectionType.NAME)
    }

    @Test
    fun selectAsAddress_byName() {
        val list = db.runQuery {
            QueryDsl.fromCommand(SelectAsAddressByName())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select address_id, street, version from address order by address_id
        """,
    )
    class SelectAsAddressDtoByIndex : Many<AddressDto> {
        override fun TemplateSelectQueryBuilder.execute() = selectAsAddressDto()
    }

    @Test
    fun selectAsAddressDto_byIndex() {
        val list = db.runQuery {
            QueryDsl.fromCommand(SelectAsAddressDtoByIndex())
        }
        assertEquals(15, list.size)
        assertEquals(AddressDto(1, "STREET 1"), list[0])
    }

    @KomapperCommand(
        """
        select street, address_id from address order by address_id
        """,
    )
    class SelectAsAddressDtoByName : Many<AddressDto> {
        override fun TemplateSelectQueryBuilder.execute() = selectAsAddressDto(ProjectionType.NAME)
    }

    @Test
    fun selectAsAddressDto_byName() {
        val list = db.runQuery {
            QueryDsl.fromCommand(SelectAsAddressDtoByName())
        }
        assertEquals(15, list.size)
        assertEquals(AddressDto(1, "STREET 1"), list[0])
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street */'', /* street.length */0)
        """,
    )
    class PropertyCall(val id: Int, val street: String) : Exec

    @Test
    fun propertyCall() {
        val count = db.runQuery {
            QueryDsl.executeCommand(PropertyCall(16, "STREET 16"))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals(9, address.version)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street.toString() */'', /* id */0)
        """,
    )
    class FunctionCall0Arg(val id: Int, val street: String) : Exec

    @Test
    fun functionCall_0Arg() {
        val count = db.runQuery {
            QueryDsl.executeCommand(FunctionCall0Arg(16, "STREET 16"))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("STREET 16", address.street)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street.equals(0) */'', /* id */0)
        """,
    )
    class FunctionCall1Arg(val id: Int, val street: String) : Exec

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun functionCall_1Arg() {
        val count = db.runQuery {
            QueryDsl.executeCommand(FunctionCall1Arg(16, "STREET 16"))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("FALSE", address.street)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street.subSequence(start, end).toString() */'', /* id */0)
        """,
    )
    class FunctionCall2Arg(val id: Int, val street: String, val start: Int, val end: Int) : Exec

    @Test
    fun functionCall_2Args() {
        val count = db.runQuery {
            QueryDsl.executeCommand(FunctionCall2Arg(16, "STREET 16", 2, 5))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("REE", address.street)
    }

    @KomapperCommand(
        """
        insert into enum_property_data 
            (id, "value") 
        values 
            (/* id */0, /*%if color == @integration.core.enumclass.Color@.BLUE */20/*%else*/null/*%end*/)
        """,
    )
    class AddEnumPropertyData(val id: Int, val color: Color) : Exec

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun enum_property() {
        db.runQuery {
            QueryDsl.executeCommand(AddEnumPropertyData(1, Color.BLUE))
        }
        val m = Meta.enumPropertyData
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(Color.BLUE, data2.value)
    }
}
