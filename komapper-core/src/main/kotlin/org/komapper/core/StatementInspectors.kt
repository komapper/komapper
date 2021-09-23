package org.komapper.core

import org.komapper.core.spi.StatementInspectorFactory
import java.util.ServiceLoader

object StatementInspectors {
    fun get(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspectorFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create() ?: DefaultStatementInspector()
    }
}
