package org.komapper.core.dsl.metamodel

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.operator.plus

/**
 * A scope for defining metamodel-related declarations.
 * This class is used internally to manage where declarations within the metamodel.
 */
@Scope
class EntityMetamodelScope {
    /**
     * The where declaration used to filter entities.
     * This is an internal property that accumulates where conditions.
     */
    internal var where: WhereDeclaration = {}

    /**
     * Adds a where declaration to the current scope.
     * This method allows chaining multiple where conditions.
     *
     * @param declaration The where declaration to be added.
     */
    fun where(declaration: WhereDeclaration) {
        where += declaration
    }
}
