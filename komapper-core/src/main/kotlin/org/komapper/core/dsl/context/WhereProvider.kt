package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.options.WhereOptions

@ThreadSafe
interface WhereProvider {
    val options: WhereOptions
    fun getCompositeWhere(): WhereDeclaration = {}
}
