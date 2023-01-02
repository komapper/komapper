package org.komapper.core.dsl.query

import org.komapper.core.dsl.element.Relationship
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.HavingDeclaration
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> set(
    @Suppress("UNUSED_PARAMETER") metamodel: META,
    declaration: AssignmentDeclaration<ENTITY, META>,
): AssignmentDeclaration<ENTITY, META> = declaration

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> values(
    @Suppress("UNUSED_PARAMETER") metamodel: META,
    declaration: AssignmentDeclaration<ENTITY, META>,
): AssignmentDeclaration<ENTITY, META> = declaration

fun on(declaration: OnDeclaration): OnDeclaration = declaration

fun where(declaration: WhereDeclaration): WhereDeclaration = declaration

fun having(declaration: HavingDeclaration): HavingDeclaration = declaration

fun orderBy(vararg expressions: SortExpression): List<SortExpression> = expressions.toList()

fun groupBy(vararg expressions: ColumnExpression<*, *>): List<ColumnExpression<*, *>> = expressions.toList()

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> join(
    metamodel: META,
    declaration: OnDeclaration,
): Relationship<ENTITY, ID, META> {
    return Relationship(metamodel, declaration)
}
