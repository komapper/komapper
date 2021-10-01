package org.komapper.core

import org.komapper.core.spi.StatementInspectorFactory
import java.util.ServiceLoader

object StatementInspectors {
    fun get(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspectorFactory::class.java)
        return loader.map { it.create() }.reduceOrNull(::decorate) ?: DefaultStatementInspector
    }

    internal fun decorate(acc: StatementInspector, inspector: StatementInspector): StatementInspector {
        return StatementInspector { statement ->
            acc.inspect(statement).let {
                inspector.inspect(it)
            }
        }
    }
}
