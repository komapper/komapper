package org.komapper.core.dsl.query

interface FlowQuery<T> {
    fun accept(visitor: FlowQueryVisitor): FlowQueryRunner
}

interface FlowableQuery<T> {
    fun toFlowQuery(): FlowQuery<T>
}
