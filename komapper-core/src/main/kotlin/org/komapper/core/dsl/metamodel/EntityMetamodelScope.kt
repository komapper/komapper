package org.komapper.core.dsl.metamodel

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.operator.plus

@Scope
class EntityMetamodelScope {
    internal var where: WhereDeclaration = {}

    fun where(declaration: WhereDeclaration) {
        where += declaration
    }
}
