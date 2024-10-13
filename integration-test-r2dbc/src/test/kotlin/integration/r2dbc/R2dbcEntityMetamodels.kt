package integration.r2dbc

import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.EntityMetamodels
import org.komapper.core.dsl.Meta
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcEntityMetamodels {
    @Test
    fun list() {
        val list = EntityMetamodels.all().map { it.second }
        assertTrue(Meta.address in list)
        assertFalse(Meta.clobData in list)
    }
}
