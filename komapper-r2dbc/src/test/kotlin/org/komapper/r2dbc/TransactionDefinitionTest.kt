package org.komapper.r2dbc

import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.TransactionDefinition
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals

class TransactionDefinitionTest {

    @Test
    fun plus_same_keys() {
        val def =
            IsolationLevel.READ_COMMITTED + IsolationLevel.SERIALIZABLE + IsolationLevel.READ_UNCOMMITTED
        assertEquals(IsolationLevel.READ_UNCOMMITTED, def.getAttribute(TransactionDefinition.ISOLATION_LEVEL))
    }

    @Test
    fun plus_different_keys() {
        val def = IsolationLevel.READ_COMMITTED +
            R2dbcTransactionName("aaa") +
            R2dbcTransactionReadOnly(true) +
            R2dbcTransactionLockWaitTimeout(Duration.ofSeconds(100))
        assertEquals(IsolationLevel.READ_COMMITTED, def[TransactionDefinition.ISOLATION_LEVEL])
        assertEquals("aaa", def[TransactionDefinition.NAME])
        assertEquals(true, def[TransactionDefinition.READ_ONLY])
        assertEquals(Duration.ofSeconds(100), def[TransactionDefinition.LOCK_WAIT_TIMEOUT])
    }
}
