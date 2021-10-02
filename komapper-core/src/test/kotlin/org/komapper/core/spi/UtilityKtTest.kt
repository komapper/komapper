package org.komapper.core.spi

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class UtilityKtTest {

    @Test
    fun findByPriority() {
        val factory1 = object : Prioritized {}
        val factory2 = object : Prioritized {
            override val priority: Int = Prioritized.defaultPriority + 1
        }
        val factory3 = object : Prioritized {
            override val priority: Int = Prioritized.defaultPriority + 2
        }
        val factory4 = object : Prioritized {
            override val priority: Int = Prioritized.defaultPriority + 2
        }
        val factory5 = object : Prioritized {
            override val priority: Int = Prioritized.defaultPriority + 1
        }
        val factories = listOf(factory1, factory2, factory3, factory4, factory5)
        assertSame(factory3, factories.findByPriority())
    }

    @Test
    fun findByPriority_empty() {
        val factories = emptyList<Prioritized>()
        assertNull(factories.findByPriority())
    }
}
