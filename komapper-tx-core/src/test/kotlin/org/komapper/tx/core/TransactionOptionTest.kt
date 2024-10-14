package org.komapper.tx.core

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TransactionOptionTest {
    @Test
    fun plus_same_keys() {
        val option =
            TransactionProperty.IsolationLevel.READ_COMMITTED +
                TransactionProperty.IsolationLevel.SERIALIZABLE +
                TransactionProperty.IsolationLevel.READ_UNCOMMITTED
        assertEquals(TransactionProperty.IsolationLevel.READ_UNCOMMITTED, option[TransactionProperty.IsolationLevel])
        println(option)
    }

    @Test
    fun plus_different_keys() {
        val option = TransactionProperty.IsolationLevel.READ_COMMITTED +
            TransactionProperty.Name("aaa") +
            TransactionProperty.ReadOnly(true)
        assertEquals(TransactionProperty.IsolationLevel.READ_COMMITTED, option[TransactionProperty.IsolationLevel])
        assertEquals(TransactionProperty.Name("aaa"), option[TransactionProperty.Name])
        assertEquals(TransactionProperty.ReadOnly(true), option[TransactionProperty.ReadOnly])
        println(option)
    }

    @Test
    fun minusKey() {
        val option = TransactionProperty.IsolationLevel.READ_COMMITTED +
            TransactionProperty.Name("aaa") +
            TransactionProperty.ReadOnly(true)
        val newOption = option.minusKey(TransactionProperty.Name)
        assertEquals(TransactionProperty.IsolationLevel.READ_COMMITTED, newOption[TransactionProperty.IsolationLevel])
        assertNull(newOption[TransactionProperty.Name])
        assertEquals(TransactionProperty.ReadOnly(true), newOption[TransactionProperty.ReadOnly])
        println(newOption)
    }

    @Test
    fun fold() {
        val option = TransactionProperty.IsolationLevel.READ_COMMITTED +
            TransactionProperty.Name("aaa") +
            TransactionProperty.ReadOnly(true) +
            TransactionProperty.LockWaitTime(Duration.ofMinutes(1L))
        val text = option.fold("") { acc, element ->
            if (acc.isEmpty()) "$element" else "$acc, $element"
        }
        assertEquals("IsolationLevel(READ_COMMITTED), Name(aaa), ReadOnly(true), LockWaitTime(PT1M)", text)
    }
}
