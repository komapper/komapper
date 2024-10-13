package integration.core

import org.junit.jupiter.api.Assertions.assertNull
import org.komapper.core.dsl.Meta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
}
