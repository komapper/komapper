package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.InnerJoin
import org.komapper.core.dsl.element.LeftJoin
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.LockOption
import org.komapper.core.dsl.expression.LockTarget
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EmptyMetamodel
import org.komapper.core.dsl.scope.ForUpdateScope

class SelectStatementBuilder(
    private val dialect: BuilderDialect,
    private val context: SelectContext<*, *, *>,
    private val aliasManager: AliasManager = DefaultAliasManager(context),
    private val projectionPredicate: (ColumnExpression<*, *>) -> Boolean = { true },
) {
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf, context.options.escapeSequence)

    fun build(): Statement {
        withClause()
        selectClause()
        fromClause()
        whereClause()
        groupByAndHavingClauses()
        orderByClause()
        offsetLimitClause()
        forUpdateClause()
        return buf.toStatement()
    }

    private fun withClause() {
        if (context.with != null && context.with.pairs.isNotEmpty()) {
            buf.append("with ")
            if (context.with.recursive && dialect.supportsRecursiveKeywordInCommonTableExpression()) {
                buf.append("recursive ")
            }
            for ((table, subquery) in context.with.pairs) {
                table(table, TableNameType.NAME_ONLY)

                if (dialect.supportsColumnNamesInCteDefinition()) {
                    buf.append(" (")
                    for (p in table.properties()) {
                        val columnName = p.getCanonicalColumnName(dialect::enquote)
                        buf.append(columnName)
                        buf.append(", ")
                    }
                    buf.cutBack(2)
                    buf.append(")")
                }
                buf.append(" as (")
                val statement = support.buildSubqueryStatement(subquery.context)
                buf.append(statement)
                buf.append("), ")
            }
            buf.cutBack(2)
            buf.append(" ")
        }
    }

    private fun selectClause() {
        buf.append("select ")
        if (context.distinct) {
            buf.append("distinct ")
        }
        for (e in context.getProjection().expressions(projectionPredicate)) {
            column(e)
            buf.append(", ")
        }
        buf.cutBack(2)
    }

    private fun fromClause() {
        if (context.target == EmptyMetamodel) {
            if (!dialect.supportsSelectStatementWithoutFromClause()) {
                buf.append(" from dual")
            }
            return
        }

        buf.append(" from ")
        if (context.derivedTable == null) {
            table(context.target)
        } else {
            fun copySubqueryContext(subqueryContext: SubqueryContext): SubqueryContext {
                return when (subqueryContext) {
                    is SelectContext<*, *, *> -> {
                        val properties = context.target.properties()
                        val columns = subqueryContext.getProjection().expressions()
                        val aliases = properties.zip(columns).map { (outer, inner) ->
                            if (inner is AliasExpression) {
                                error("The alias operand is found. Komapper does not allow the use of alias operand in derived tables.")
                            } else {
                                AliasExpression(inner, outer.columnName, outer.alwaysQuote)
                            }
                        }
                        subqueryContext.copy(select = aliases)
                    }

                    is SetOperationContext -> {
                        subqueryContext.copy(
                            left = copySubqueryContext(subqueryContext.left),
                            right = copySubqueryContext(subqueryContext.right),
                        )
                    }
                }
            }

            val subqueryContext = copySubqueryContext(context.derivedTable.context)
            val statement = support.buildSubqueryStatement(subqueryContext)
            buf.append("(")
            buf.append(statement)
            buf.append(")")
            val alias = aliasManager.getAlias(context.target)
            if (!alias.isNullOrBlank()) {
                if (dialect.supportsAsKeywordForTableAlias()) {
                    buf.append(" as")
                }
                buf.append(" $alias")
            }
        }

        if (dialect.supportsTableHint() && context.forUpdate != null) {
            val scope = ForUpdateScope().apply(context.forUpdate)
            buf.append(" with (updlock, rowlock")
            when (scope.lockOption) {
                is LockOption.Nowait -> if (dialect.supportsLockOptionNowait()) {
                    buf.append(", nowait")
                } else {
                    throw UnsupportedOperationException("The dialect(driver=${dialect.driver}) does not support the nowait option. sql=$buf")
                }

                else -> Unit
            }
            buf.append(")")
        }

        if (context.joins.isNotEmpty()) {
            for (join in context.joins) {
                when (join) {
                    is InnerJoin -> buf.append(" inner join ")
                    is LeftJoin -> buf.append(" left outer join ")
                }
                table(join.target)
                val criteria = join.getOnCriteria()
                if (criteria.isNotEmpty()) {
                    buf.append(" on (")
                    for ((index, criterion) in criteria.withIndex()) {
                        criterion(index, criterion)
                        buf.append(" and ")
                    }
                    buf.cutBack(5)
                    buf.append(")")
                }
            }
        }
    }

    private fun whereClause() {
        val criteria = context.getWhereCriteria()
        if (criteria.isNotEmpty()) {
            buf.append(" where ")
            for ((index, criterion) in criteria.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
    }

    private fun groupByAndHavingClauses() {
        val havingCriteria = context.getHavingCriteria()
        val expressions = context.getProjection().expressions()
        val aggregateFunctions = expressions.filterIsInstance<AggregateFunction<*, *>>()
        val groupByItems = if (context.groupBy.isNotEmpty()) {
            context.groupBy
        } else if (havingCriteria.isNotEmpty() || aggregateFunctions.isNotEmpty()) {
            expressions - aggregateFunctions.toSet()
        } else {
            emptyList()
        }
        if (groupByItems.isNotEmpty()) {
            buf.append(" group by ")
            for (item in groupByItems) {
                column(item)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        if (havingCriteria.isNotEmpty()) {
            buf.append(" having ")
            for ((index, criterion) in havingCriteria.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
    }

    private fun orderByClause() {
        val orderBy = context.orderBy.ifEmpty {
            if ((context.offset >= 0 || context.limit > 0) &&
                !dialect.supportsLimitOffsetWithoutOrderByClause()
            ) {
                context.getProjection().expressions().map(SortItem.Column::of)
            } else {
                emptyList()
            }
        }
        val orderBySupport = OrderByBuilderSupport(dialect, orderBy, aliasManager, buf)
        orderBySupport.orderByClause()
    }

    private fun offsetLimitClause() {
        val builder = dialect.getOffsetLimitStatementBuilder(dialect, context.offset, context.limit)
        val statement = builder.build()
        buf.append(statement)
    }

    private fun forUpdateClause() {
        if (dialect.supportsForUpdateClause() && context.forUpdate != null) {
            val scope = ForUpdateScope().apply(context.forUpdate)
            buf.append(" for update")
            lockTarget(scope.lockTarget)
            lockOption(scope.lockOption)
        }
    }

    private fun lockTarget(lockTarget: LockTarget) {
        when (lockTarget) {
            is LockTarget.Empty -> Unit
            is LockTarget.Metamodels -> {
                when (true) {
                    dialect.supportsLockOfColumns() -> {
                        if (lockTarget.metamodels.isNotEmpty()) {
                            buf.append(" of ")
                            for (column in lockTarget.metamodels.map { it.idProperties().first() }) {
                                support.visitColumnExpression(column)
                                buf.append(", ")
                            }
                            buf.cutBack(2)
                        }
                    }

                    dialect.supportsLockOfTables() -> {
                        if (lockTarget.metamodels.isNotEmpty()) {
                            buf.append(" of ")
                            for (table in lockTarget.metamodels) {
                                support.visitTableExpression(table, TableNameType.ALIAS_ONLY)
                                buf.append(", ")
                            }
                            buf.cutBack(2)
                        }
                    }

                    else -> throw UnsupportedOperationException("The dialect(driver=${dialect.driver}) does not support the \"for update of\" syntax. sql=$buf")
                }
            }
        }
    }

    private fun lockOption(lockOption: LockOption) {
        fun raiseError(optionName: String) {
            throw UnsupportedOperationException("The dialect(driver=${dialect.driver}) does not support the $optionName option. sql=$buf")
        }
        when (lockOption) {
            is LockOption.Default -> Unit
            is LockOption.Nowait -> if (dialect.supportsLockOptionNowait()) {
                buf.append(" nowait")
            } else {
                raiseError("nowait")
            }

            is LockOption.SkipLocked -> if (dialect.supportsLockOptionSkipLocked()) {
                buf.append(" skip locked")
            } else {
                raiseError("skip locked")
            }

            is LockOption.Wait -> if (dialect.supportsLockOptionWait()) {
                buf.append(" wait ${lockOption.second}")
            } else {
                raiseError("wait")
            }
        }
    }

    private fun table(expression: TableExpression<*>, tableNameType: TableNameType = TableNameType.NAME_AND_ALIAS) {
        support.visitTableExpression(expression, tableNameType)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }

    private fun criterion(index: Int, c: Criterion) {
        support.visitCriterion(index, c)
    }
}

internal class OrderByBuilderSupport(
    private val dialect: BuilderDialect,
    private val sortItems: List<SortItem>,
    aliasManager: AliasManager,
    private val buf: StatementBuffer,
) {
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun orderByClause() {
        if (sortItems.isNotEmpty()) {
            buf.append(" order by ")
            for (item in sortItems) {
                when (item) {
                    is SortItem.Column -> {
                        val appendColumn = { column(item.expression) }
                        when (item) {
                            is SortItem.Column.Asc -> asc(appendColumn)
                            is SortItem.Column.AscNullsFirst -> ascNullsFirst(appendColumn)
                            is SortItem.Column.AscNullsLast -> ascNullsLast(appendColumn)
                            is SortItem.Column.Desc -> desc(appendColumn)
                            is SortItem.Column.DescNullsFirst -> descNullsFirst(appendColumn)
                            is SortItem.Column.DescNullsLast -> descNullsLast(appendColumn)
                        }
                    }

                    is SortItem.Alias -> {
                        val appendColumn: () -> Unit = { buf.append(dialect.enquote(item.alias)) }
                        when (item) {
                            is SortItem.Alias.Asc -> asc(appendColumn)
                            is SortItem.Alias.AscNullsFirst -> ascNullsFirst(appendColumn)
                            is SortItem.Alias.AscNullsLast -> ascNullsLast(appendColumn)
                            is SortItem.Alias.Desc -> desc(appendColumn)
                            is SortItem.Alias.DescNullsFirst -> descNullsFirst(appendColumn)
                            is SortItem.Alias.DescNullsLast -> descNullsLast(appendColumn)
                        }
                    }
                }
                buf.append(", ")
            }
            buf.cutBack(2)
        }
    }

    private fun column(expression: ColumnExpression<*, *>) {
        support.visitColumnExpression(expression)
    }

    private fun asc(appendColumn: () -> Unit) {
        appendColumn()
        buf.append(" asc")
    }

    private fun desc(appendColumn: () -> Unit) {
        appendColumn()
        buf.append(" desc")
    }

    private fun ascNullsFirst(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" asc nulls first")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 0 else 1 end asc, ")
            asc(appendColumn)
        }
    }

    private fun ascNullsLast(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" asc nulls last")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 1 else 0 end asc, ")
            asc(appendColumn)
        }
    }

    private fun descNullsFirst(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" desc nulls first")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 1 else 0 end desc, ")
            desc(appendColumn)
        }
    }

    private fun descNullsLast(appendColumn: () -> Unit) {
        if (dialect.supportsNullOrdering()) {
            appendColumn()
            buf.append(" desc nulls last")
        } else {
            buf.append("case when ")
            appendColumn()
            buf.append(" is null then 0 else 1 end desc, ")
            desc(appendColumn)
        }
    }
}
