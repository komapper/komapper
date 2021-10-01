package org.komapper.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.spi.Prioritized
import org.komapper.core.spi.StatementInspectorFactory

internal class StatementInspectorsTest {

    @Test
    fun compose() {
        val factory1 = object : StatementInspectorFactory {
            override fun create(): StatementInspector = StatementInspector { it + "1" }
        }
        val factory2 = object : StatementInspectorFactory {
            override val priority: Int = Prioritized.defaultPriority + 1
            override fun create(): StatementInspector = StatementInspector { it + "2" }
        }
        val factory3 = object : StatementInspectorFactory {
            override fun create(): StatementInspector = StatementInspector { it + "3" }
        }

        val factories = listOf(factory1, factory2, factory3)
        val inspector = StatementInspectors.compose(factories)
        val statement = inspector.inspect(Statement("hello"))
        assertEquals("hello213", statement.toSql())
    }

    @Test
    fun compose_empty() {
        val inspector = StatementInspectors.compose(emptyList())
        val statement = inspector.inspect(Statement("hello"))
        assertEquals("hello", statement.toSql())
    }
}
