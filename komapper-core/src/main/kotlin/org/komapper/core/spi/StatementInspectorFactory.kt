package org.komapper.core.spi

import org.komapper.core.DefaultStatementInspector
import org.komapper.core.StatementInspector
import org.komapper.core.ThreadSafe
import java.util.ServiceLoader

@ThreadSafe
interface StatementInspectorFactory {
    fun create(): StatementInspector
}

object StatementInspectorProvider {
    fun get(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspectorFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create() ?: DefaultStatementInspector()
    }
}
