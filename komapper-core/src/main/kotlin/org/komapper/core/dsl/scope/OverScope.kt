package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.WindowFrame
import org.komapper.core.dsl.expression.WindowFrameBound
import org.komapper.core.dsl.expression.WindowFrameExclusion
import org.komapper.core.dsl.expression.WindowFrameKind

@Scope
interface OverScope {
    val currentRow: WindowFrameBound get() = WindowFrameBound.CurrentRow
    val unboundedPreceding: WindowFrameBound get() = WindowFrameBound.UnboundedPreceding
    val unboundedFollowing: WindowFrameBound get() = WindowFrameBound.UnboundedFollowing
    fun preceding(offset: Int): WindowFrameBound.Preceding = WindowFrameBound.Preceding(offset)
    fun following(offset: Int): WindowFrameBound.Following = WindowFrameBound.Following(offset)

    fun partitionBy(vararg expressions: ColumnExpression<*, *>)
    fun orderBy(vararg expressions: SortExpression)
    fun groups(start: WindowFrameBound, end: WindowFrameBound? = null, exclusion: WindowFrameExclusion? = null)
    fun range(start: WindowFrameBound, end: WindowFrameBound? = null, exclusion: WindowFrameExclusion? = null)
    fun rows(start: WindowFrameBound, end: WindowFrameBound? = null, exclusion: WindowFrameExclusion? = null)
}

internal class OverScopeImpl : OverScope {
    val partitionBy: MutableList<ColumnExpression<*, *>> = mutableListOf()
    val orderBy: MutableList<SortItem> = mutableListOf()
    var frame: WindowFrame? = null

    override fun partitionBy(vararg expressions: ColumnExpression<*, *>) {
        partitionBy.addAll(expressions.toList())
    }

    override fun orderBy(vararg expressions: SortExpression) {
        val items = expressions.map(SortItem.Column::of)
        orderBy.addAll(items)
    }

    override fun groups(start: WindowFrameBound, end: WindowFrameBound?, exclusion: WindowFrameExclusion?) {
        this.frame = WindowFrame(WindowFrameKind.GROUPS, start, end, exclusion)
    }

    override fun range(start: WindowFrameBound, end: WindowFrameBound?, exclusion: WindowFrameExclusion?) {
        this.frame = WindowFrame(WindowFrameKind.RANGE, start, end, exclusion)
    }

    override fun rows(start: WindowFrameBound, end: WindowFrameBound?, exclusion: WindowFrameExclusion?) {
        this.frame = WindowFrame(WindowFrameKind.ROWS, start, end, exclusion)
    }
}
