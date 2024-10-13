package integration.jdbc

import integration.core.MyMeta
import integration.core.address
import integration.core.myAddress
import integration.core.myPerson
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityMetamodels
import org.komapper.core.dsl.Meta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcEntityMetamodels {
    @Test
    fun all() {
        val list = EntityMetamodels.all().map { it.second }
        assertTrue(Meta.address in list)
        assertTrue(Meta.sqlXmlData in list)
        assertTrue(MyMeta.myAddress in list)
        assertTrue(MyMeta.myPerson in list)
    }

    @Test
    fun listByEntityUnit() {
        val list = EntityMetamodels.list(MyMeta)
        assertFalse(Meta.address in list)
        assertFalse(Meta.sqlXmlData in list)
        assertTrue(MyMeta.myAddress in list)
        assertTrue(MyMeta.myPerson in list)
    }

    @Test
    fun all_Meta() {
        assertEquals(EntityMetamodels.list(Meta), Meta.all())
    }
}
