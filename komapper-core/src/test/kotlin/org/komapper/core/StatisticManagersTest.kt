package org.komapper.core

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatisticManagersTest {
    @Test
    fun get_enable_true() {
        val result = StatisticManagers.get(true)
        assertTrue(result is DefaultStatisticManager)
        assertTrue(result.isEnabled())
    }

    @Test
    fun get_enable_false() {
        val result = StatisticManagers.get(false)
        assertTrue(result is DefaultStatisticManager)
        assertFalse(result.isEnabled())
    }
}
