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
import org.komapper.annotation.KomapperPartial
import org.komapper.annotation.KomapperUnused
import org.komapper.core.Exec
import org.komapper.core.ExecReturnMany
import org.komapper.core.ExecReturnOne
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
import kotlin.test.assertTrue

@KomapperPartial(
    """
    /*%if pagination != null */
    limit /* pagination.limit */0 offset /*pagination.offset*/0
    /*%end*/
    """,
)
private const val paginationPartial = ""

@ExtendWith(JdbcEnv::class)
class JdbcCommandTest(private val db: JdbcDatabase) {

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street */'', /* version */0)
        returning
            address_id, street, version
        """,
    )
    class AddAddressThenReturn(val id: Int, val street: String, val version: Int) : ExecReturnOne<Address>({ select(asAddress).single() })

    @Test
    @Run(unless = [Dbms.H2, Dbms.MYSQL, Dbms.MYSQL_5, Dbms.SQLSERVER, Dbms.ORACLE])
    fun addAddressThenReturn() {
        val address = db.runQuery {
            QueryDsl.execute(AddAddressThenReturn(16, "STREET 16", 1))
        }
        assertEquals(
            Address(
                16,
                "STREET 16",
                1,
            ),
            address,
        )
    }

    @KomapperCommand(
        """
        update address set 
            version = version + 1 
        returning
            address_id, street, version
        """,
    )
    class IncrementAddressVersionThenReturn : ExecReturnMany<Address>({ select(asAddress) })

    @Test
    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.MYSQL, Dbms.MYSQL_5, Dbms.SQLSERVER, Dbms.ORACLE])
    fun incrementAddressVersionThenReturn() {
        val addresses = db.runQuery {
            QueryDsl.execute(IncrementAddressVersionThenReturn())
        }
        assertEquals(15, addresses.size)
        assertTrue(addresses.all { it.version == 2 })
    }

    @KomapperCommand(
        """
        select * from address where /*%if street != null*/ street = /*street*/'test' /*%end*/
        """,
    )
    class Bind(val street: String) : One<Address>({ select(asAddress).single() })

    @Test
    fun bind() {
        val address = db.runQuery {
            QueryDsl.execute(Bind("STREET 10"))
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
    data class BindNull(val street: String?) : Many<Address>({ select(asAddress) })

    @Test
    fun bindNull() {
        val list = db.runQuery {
            QueryDsl.execute(BindNull(null))
        }
        assertEquals(15, list.size)
    }

    @KomapperCommand(
        """
        select * from address where address_id in /*list*/(0)
        """,
    )
    data class In(val list: List<Int>) : Many<Address>({ select(asAddress) })

    @Test
    fun `in`() {
        val list = db.runQuery {
            QueryDsl.execute(In(listOf(1, 2)))
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
    data class In2(val pairs: List<Pair<Int, String>>) : Many<Address>({ select(asAddress) })

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in2() {
        val list = db.runQuery {
            QueryDsl.execute(In2(listOf(1 to "STREET 1", 2 to "STREET 2")))
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
    data class In3(val triples: List<Triple<Int, String, Int>>) : Many<Address>({ select(asAddress) })

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun in3() {
        val list = db.runQuery {
            QueryDsl.execute(
                In3(
                    listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1),
                    ),
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

    @KomapperCommand(
        """
        select * from address 
        where street like concat(/* street.escape() */'test', '%')
        order by address_id
        """,
    )
    data class Escape(val street: String, @KomapperUnused val asAddress: (Row) -> Address) : Many<Address>({ select(asAddress) }) {
        override fun TemplateSelectQueryBuilder.execute() = select(asAddress)
    }

    @Test
    fun escape() {
        val list = db.runQuery {
            QueryDsl.execute(Escape("STREET 1", asAddress))
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @KomapperCommand(
        """
        select * from address 
        where street like concat(/* street.asPrefix() */'test', '%')
        order by address_id
        """,
        functionName = "fetchUsingAsPrefix",
    )
    data class AsPrefix(val street: String) : Many<Address>({ select(asAddress) })

    @Test
    fun asPrefix() {
        val list = db.runQuery {
            QueryDsl.fetchUsingAsPrefix(AsPrefix("STREET 1"))
        }
        assertEquals((listOf(1) + (10..15)).toList(), list.map { it.addressId })
    }

    @KomapperCommand(
        """
        update address set street = /*street*/'' where address_id = /*id*/0
        """,
    )
    data class Execute(val id: Int, val street: String) : Exec()

    @Test
    fun execute() {
        val count = db.runQuery {
            QueryDsl.execute(Execute(15, "NY street"))
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
    class SelectAsEntityByIndex : Many<Address>({ selectAsEntity(Meta.address) })

    @Test
    fun selectAsEntity_byIndex() {
        val list = db.runQuery {
            QueryDsl.execute(SelectAsEntityByIndex())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select street, version, address_id from address order by address_id
        """,
    )
    class SelectAsEntityByName : Many<Address>({ selectAsEntity(Meta.address, ProjectionType.NAME) })

    @Test
    fun selectAsEntity_byName() {
        val list = db.runQuery {
            QueryDsl.execute(SelectAsEntityByName())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select address_id, street, version from address order by address_id
        """,
    )
    class SelectAsAddressByIndex : Many<Address>({ selectAsAddress() })

    @Test
    fun selectAsAddress_byIndex() {
        val list = db.runQuery {
            QueryDsl.execute(SelectAsAddressByIndex())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select street, version, address_id from address order by address_id
        """,
    )
    class SelectAsAddressByName : Many<Address>({ selectAsAddress(ProjectionType.NAME) })

    @Test
    fun selectAsAddress_byName() {
        val list = db.runQuery {
            QueryDsl.execute(SelectAsAddressByName())
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @KomapperCommand(
        """
        select address_id, street, version from address order by address_id
        """,
    )
    class SelectAsAddressDtoByIndex : Many<AddressDto>({ selectAsAddressDto() })

    @Test
    fun selectAsAddressDto_byIndex() {
        val list = db.runQuery {
            QueryDsl.execute(SelectAsAddressDtoByIndex())
        }
        assertEquals(15, list.size)
        assertEquals(AddressDto(1, "STREET 1"), list[0])
    }

    @KomapperCommand(
        """
        select street, address_id from address order by address_id
        """,
    )
    class SelectAsAddressDtoByName : Many<AddressDto>({ selectAsAddressDto(ProjectionType.NAME) })

    @Test
    fun selectAsAddressDto_byName() {
        val list = db.runQuery {
            QueryDsl.execute(SelectAsAddressDtoByName())
        }
        assertEquals(15, list.size)
        assertEquals(AddressDto(1, "STREET 1"), list[0])
    }

    @KomapperCommand(
        """
        select 
            address_id, street, version 
        from 
            address 
        where
            /*%for street in streets */
            street = /*street*/''
            /*%if street_has_next */
            /*# "or"*/
            /*%end*/
            /*%end*/
        order by 
            address_id
        """,
    )
    class ForDirective(val streets: List<String>) : Many<Address>({ selectAsAddress() })

    @Test
    fun forDirective() {
        val list = db.runQuery {
            QueryDsl.execute(ForDirective(listOf("STREET 1", "STREET 3", "STREET 5")))
        }
        assertEquals(3, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
        assertEquals(Address(3, "STREET 3", 1), list[1])
        assertEquals(Address(5, "STREET 5", 1), list[2])
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street */'', /* street.length */0)
        """,
    )
    class PropertyCall(val id: Int, val street: String) : Exec()

    @Test
    fun propertyCall() {
        val count = db.runQuery {
            QueryDsl.execute(PropertyCall(16, "STREET 16"))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("STREET 16".length, address.version)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street */'', /* street.lastIndex */0)
        """,
    )
    class ExtensionPropertyCall(val id: Int, val street: String) : Exec()

    @Test
    fun extensionPropertyCall() {
        val count = db.runQuery {
            QueryDsl.execute(ExtensionPropertyCall(16, "STREET 16"))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("STREET 16".lastIndex, address.version)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street.toString() */'', /* id */0)
        """,
    )
    class FunctionCall0Arg(val id: Int, val street: String) : Exec()

    @Test
    fun functionCall_0Arg() {
        val count = db.runQuery {
            QueryDsl.execute(FunctionCall0Arg(16, "STREET 16"))
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
    class FunctionCall1Arg(val id: Int, val street: String) : Exec()

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun functionCall_1Arg() {
        val count = db.runQuery {
            QueryDsl.execute(FunctionCall1Arg(16, "STREET 16"))
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
    class FunctionCall2Arg(val id: Int, val street: String, val start: Int, val end: Int) : Exec()

    @Test
    fun functionCall_2Args() {
        val count = db.runQuery {
            QueryDsl.execute(FunctionCall2Arg(16, "STREET 16", 2, 5))
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
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* street.isBlank() */'', /* id */0)
        """,
    )
    class ExtensionFunctionCall0Arg(val id: Int, val street: String) : Exec()

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun extensionFunctionCall_0Arg() {
        val count = db.runQuery {
            QueryDsl.execute(ExtensionFunctionCall0Arg(16, "STREET 16"))
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
        insert into enum_property_data 
            (id, "value") 
        values 
            (/* id */0, /*%if color == @integration.core.enumclass.Color@.BLUE */20/*%else*/null/*%end*/)
        """,
    )
    class EnumProperty(val id: Int, val color: Color) : Exec()

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun enum_property() {
        db.runQuery {
            QueryDsl.execute(EnumProperty(1, Color.BLUE))
        }
        val m = Meta.enumPropertyData
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(Color.BLUE, data2.value)
    }

    @KomapperCommand(
        """
        insert into enum_property_data 
            (id, "value") 
        values 
            (/*%if @integration.core.enumclass.Color@.values() != null */1/*%else*/0/*%end*/, /*%if color == @integration.core.enumclass.Color@.valueOf("BLUE") */20/*%else*/null/*%end*/)
        """,
    )
    class EnumFunction(val color: Color) : Exec()

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun enum_function() {
        db.runQuery {
            QueryDsl.execute(EnumFunction(Color.BLUE))
        }
        val m = Meta.enumPropertyData
        val data = db.runQuery {
            QueryDsl.from(m).first()
        }
        assertEquals(1, data.id)
        assertEquals(Color.BLUE, data.value)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* @integration.jdbc.Hello@.name */'', /* id */0)
        """,
    )
    class CompanionObjectPropertyCall(val id: Int) : Exec()

    @Test
    fun companionObjectPropertyCall() {
        val count = db.runQuery {
            QueryDsl.execute(CompanionObjectPropertyCall(16))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("hello", address.street)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* @integration.jdbc.Hello@.greet("world") */'', /* id */0)
        """,
    )
    class CompanionObjectFunctionCall(val id: Int) : Exec()

    @Test
    fun companionObjectFunctionCall() {
        val count = db.runQuery {
            QueryDsl.execute(CompanionObjectFunctionCall(16))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("hello world!", address.street)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* @integration.jdbc.Hi@.name */'', /* id */0)
        """,
    )
    class ObjectPropertyCall(val id: Int) : Exec()

    @Test
    fun objectPropertyCall() {
        val count = db.runQuery {
            QueryDsl.execute(ObjectPropertyCall(16))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("hi", address.street)
    }

    @KomapperCommand(
        """
        insert into address 
            (address_id, street, version) 
        values 
            (/* id */0, /* @integration.jdbc.Hi@.greet("world") */'', /* id */0)
        """,
    )
    class ObjectFunctionCall(val id: Int) : Exec()

    @Test
    fun objectFunctionCall() {
        val count = db.runQuery {
            QueryDsl.execute(ObjectFunctionCall(16))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("hi world!", address.street)
    }

    data class Pagination(val limit: Int, val offset: Int)

    @KomapperCommand(
        """
        select * from address order by address_id
        /*> paginationPartial */
        """,
    )
    class UsePartial(val pagination: Pagination?) : Many<Address>({ selectAsAddress() })

    @Test
    @Run(unless = [Dbms.SQLSERVER, Dbms.ORACLE])
    fun usePartial() {
        val addresses = db.runQuery {
            QueryDsl.execute(UsePartial(Pagination(2, 3)))
        }
        assertEquals(2, addresses.size)
        println(addresses)
        assertEquals(listOf(4, 5), addresses.map { it.addressId })
    }

    abstract class GetSingleAddress(@KomapperUnused val unknown: Int) : One<Address>({ select(asAddress).single() })

    @KomapperCommand(
        """
        select * from address where /*%if street != null*/ street = /*street*/'test' /*%end*/
        """,
    )
    class GetSingleAddressByStreet(val street: String) : GetSingleAddress(1)

    @Test
    fun inheritance() {
        val address = db.runQuery {
            QueryDsl.execute(GetSingleAddressByStreet("STREET 10"))
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

    /* TODO
    @KomapperCommand(
        """
        insert into address
            (address_id, street, version)
        values
            (/* id */0, /* f() */'', /* id */0)
        """,
    )
    class FunctionPassing(val id: Int, val f: () -> String) : Exec

    @Test
    fun functionPassing() {
        val count = db.runQuery {
            QueryDsl.execute(FunctionPassing(16, {"good"}))
        }
        assertEquals(1, count)
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq 16 }.single()
        }
        assertEquals("good", address.street)
    }
     */
}

@Suppress("UNUSED")
class Hello {
    fun say(name: String): String {
        return "hello $name"
    }

    fun say(name: String, message: String): String {
        return "hello $name, $message"
    }

    companion object {
        const val name = "hello"

        const val constName = "hello const"

        fun greet(name: String): String {
            return "hello $name!"
        }
    }
}

object Hi {
    val name = "hi"

    const val constName = "hi const"

    @Suppress("unused")
    fun greet(name: String): String {
        return "hi $name!"
    }
}

private val asAddress: (Row) -> Address = { row ->
    Address(
        row.getNotNull("address_id"),
        row.getNotNull("street"),
        row.getNotNull("version"),
    )
}
