package org.komapper.core.query

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.WhereDeclaration

interface SqlSelectQuery<ENTITY> : Query<List<ENTITY>> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg sortItems: PropertyMetamodel<*, *>): SqlSelectQuery<ENTITY>
    fun offset(value: Int): SqlSelectQuery<ENTITY>
    fun limit(value: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
}
