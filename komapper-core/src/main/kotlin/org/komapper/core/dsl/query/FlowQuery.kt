package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.runner.FlowQueryRunner
import org.komapper.core.dsl.visitor.FlowQueryVisitor

@ThreadSafe
fun interface FlowQuery<T> {
    fun accept(visitor: FlowQueryVisitor): FlowQueryRunner
}
