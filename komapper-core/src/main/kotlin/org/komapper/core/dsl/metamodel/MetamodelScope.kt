package org.komapper.core.dsl.metamodel

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.WhereDeclaration

@Scope
class MetamodelScope<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> {
    private val _where: MutableList<WhereDeclaration> = mutableListOf()
    internal val where: List<WhereDeclaration> get() = _where

    fun where(declaration: WhereDeclaration) {
        _where.add(declaration)
    }
}
