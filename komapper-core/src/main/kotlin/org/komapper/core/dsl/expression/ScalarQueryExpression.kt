package org.komapper.core.dsl.expression

interface ScalarQueryExpression<A, B : Any, C : Any> :
    SubqueryExpression<A>, ScalarExpression<B, C>
