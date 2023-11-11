package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.SubqueryExpression

data class DerivedTableMetamodel<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, SUBQUERY_RESULT>(
    val metamodel: META,
    val subquery: SubqueryExpression<SUBQUERY_RESULT>,
) : EntityMetamodel<ENTITY, ID, META> by metamodel, SubqueryExpression<SUBQUERY_RESULT> by subquery
