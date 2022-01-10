package org.komapper.core

import org.komapper.core.spi.StatementInspectorFactory
import org.komapper.core.spi.findByPriority
import java.util.ServiceLoader

/**
 * The provider of [StatementInspector].
 */
@ThreadSafe
object StatementInspectors {
    fun get(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspectorFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create() ?: DefaultStatementInspector
    }
}
