package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.TransactionDefinition
import org.komapper.tx.core.TransactionProperty
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcUtilityTest {
    @Test
    fun standardStyle() {
        val property = TransactionProperty.IsolationLevel.READ_COMMITTED
        assertEquals(
            TransactionProperty.IsolationLevel.READ_COMMITTED,
            property[TransactionProperty.IsolationLevel],
        )
        val definition = property.asDefinition()
        assertEquals(IsolationLevel.READ_COMMITTED, definition.getAttribute(TransactionDefinition.ISOLATION_LEVEL))
    }

    @Test
    fun r2dbcStyle() {
        val property = defineTransactionProperty(TransactionDefinition.ISOLATION_LEVEL, IsolationLevel.READ_COMMITTED)
        assertEquals(
            TransactionProperty.IsolationLevel.READ_COMMITTED,
            property[TransactionProperty.IsolationLevel],
        )
        val definition = property.asDefinition()
        assertEquals(IsolationLevel.READ_COMMITTED, definition.getAttribute(TransactionDefinition.ISOLATION_LEVEL))
    }

    @Test
    fun plus_same_keys() {
        val prop1 = defineTransactionProperty(TransactionDefinition.ISOLATION_LEVEL, IsolationLevel.READ_COMMITTED)
        val prop2 = defineTransactionProperty(TransactionDefinition.ISOLATION_LEVEL, IsolationLevel.SERIALIZABLE)
        val prop = prop1 + prop2
        assertEquals(
            TransactionProperty.IsolationLevel.SERIALIZABLE,
            prop[TransactionProperty.IsolationLevel],
        )
    }

    @Test
    fun plus_different_keys() {
        val prop1 = defineTransactionProperty(TransactionDefinition.ISOLATION_LEVEL, IsolationLevel.READ_COMMITTED)
        val prop2 = defineTransactionProperty(TransactionDefinition.NAME, "aaa")
        val prop3 = defineTransactionProperty(TransactionDefinition.READ_ONLY, true)
        val prop4 = defineTransactionProperty(TransactionDefinition.LOCK_WAIT_TIMEOUT, Duration.ofMinutes(1L))
        val prop = prop1 + prop2 + prop3 + prop4
        assertEquals(TransactionProperty.IsolationLevel.READ_COMMITTED, prop[TransactionProperty.IsolationLevel])
        assertEquals("aaa", prop[TransactionProperty.Name]?.value)
        assertEquals(true, prop[TransactionProperty.ReadOnly]?.value)
        assertEquals(Duration.ofMinutes(1L), prop[TransactionProperty.LockWaitTime]?.value)
    }

    @Test
    fun minusKey() {
        val prop1 = defineTransactionProperty(TransactionDefinition.ISOLATION_LEVEL, IsolationLevel.READ_COMMITTED)
        val prop2 = defineTransactionProperty(TransactionDefinition.NAME, "aaa")
        val prop3 = defineTransactionProperty(TransactionDefinition.READ_ONLY, true)
        val prop4 = defineTransactionProperty(TransactionDefinition.LOCK_WAIT_TIMEOUT, Duration.ofMinutes(1L))
        val prop = prop1 + prop2 + prop3 + prop4
        val newProp = prop.minusKey(TransactionDefinition.READ_ONLY.asKey())
        assertEquals(TransactionProperty.IsolationLevel.READ_COMMITTED, newProp[TransactionProperty.IsolationLevel])
        assertEquals("aaa", newProp[TransactionProperty.Name]?.value)
        assertNull(newProp[TransactionProperty.ReadOnly]?.value)
        assertEquals(Duration.ofMinutes(1L), newProp[TransactionProperty.LockWaitTime]?.value)
    }
}
