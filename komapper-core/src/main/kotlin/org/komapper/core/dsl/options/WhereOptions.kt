package org.komapper.core.dsl.options

interface WhereOptions : QueryOptions {
    val allowEmptyWhereClause: Boolean
    val escapeSequence: String?
}
