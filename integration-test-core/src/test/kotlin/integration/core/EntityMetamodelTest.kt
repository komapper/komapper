package integration.core

import org.komapper.core.dsl.Meta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class EntityMetamodelTest {
    @Test
    fun get() {
        val a = Meta.address
        assertEquals(a.addressId, a["addressId"])
        assertEquals(a.street, a["street"])
        assertNull(a["unknown"])

        val b = Meta.address.clone()
        assertEquals(b.addressId, b["addressId"])
        assertEquals(b.street, b["street"])
        assertNull(b["unknown"])

        assertNotEquals(a, b)
    }

    @Test
    fun properties() {
        val a = Meta.address
        assertEquals(a.addressId, a.properties().find { it.name == "addressId" })
        assertEquals(a.street, a.properties().find { it.name == "street" })
        assertNull(a.properties().find { it.name == "unknown" })

        val b = Meta.address.clone()
        assertEquals(b.addressId, b.properties().find { it.name == "addressId" })
        assertEquals(b.street, b.properties().find { it.name == "street" })
        assertNull(b.properties().find { it.name == "unknown" })

        assertNotEquals(a, b)
    }

    @Test
    fun toText() {
        val a = Meta.address
        val address = Address(1, "aaa", 2)
        assertEquals("Address(addressId=1, street=aaa, version=2)", a.toText(address))
    }

    @Test
    fun `toText - masking`() {
        val s = Meta.spot
        val spot = Spot(1, "aaa", 2)
        assertEquals("Spot(id=1, street=*****, version=2)", s.toText(spot))
    }

    @Test
    fun `toText - null`() {
        val s = Meta.site
        val site = Site(1, null, 2)
        assertEquals("Site(id=1, street=null, version=2)", s.toText(site))
    }

    @Test
    fun `toText - separate definition`() {
        val c = Meta.cyborg
        val cyborg = Cyborg(
            employeeId = 99,
            managerId = null,
            departmentId = 1,
            addressId = 1,
            version = 0,
            info1 = 9999 to "a",
            info2 = null,
        )
        assertEquals(
            "Cyborg(employeeId=99, first=9999, second=a, first=null, second=null, managerId=null, departmentId=1, addressId=1, version=0)",
            c.toText(cyborg)
        )
    }
}
