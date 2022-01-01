package org.komapper.core

import org.komapper.core.spi.StatementInspectorFactory
import java.util.ServiceLoader

/**
 * The provider of [StatementInspector].
 */
@ThreadSafe
object StatementInspectors {
    /**
     * @return the [StatementInspector]
     */
    fun get(): StatementInspector {
        val loader = ServiceLoader.load(StatementInspectorFactory::class.java)
        return compose(loader)
    }

    internal fun compose(factories: Iterable<StatementInspectorFactory>): StatementInspector {
        return factories
            .sortedByDescending { it.priority }
            .map { it.create() }
            .reduceOrNull { acc, inspector ->
                StatementInspector { statement ->
                    acc.inspect(statement).let {
                        inspector.inspect(it)
                    }
                }
            } ?: DefaultStatementInspector
    }
}
