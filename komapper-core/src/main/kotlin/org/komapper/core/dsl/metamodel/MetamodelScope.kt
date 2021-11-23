package org.komapper.core.dsl.metamodel

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.operator.plus

@Scope
class MetamodelScope<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    internal var where: WhereDeclaration = {}

    fun where(declaration: WhereDeclaration) {
        where += declaration
    }
}
