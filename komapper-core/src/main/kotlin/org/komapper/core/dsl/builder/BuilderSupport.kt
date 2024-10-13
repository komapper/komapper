package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.LocateFunctionType
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ArithmeticExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ConditionalExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.CumeDist
import org.komapper.core.dsl.expression.DenseRank
import org.komapper.core.dsl.expression.DistinctExpression
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.FirstValue
import org.komapper.core.dsl.expression.Lag
import org.komapper.core.dsl.expression.LastValue
import org.komapper.core.dsl.expression.Lead
import org.komapper.core.dsl.expression.LiteralExpression
import org.komapper.core.dsl.expression.MathematicalFunction
import org.komapper.core.dsl.expression.NonNullLiteralExpression
import org.komapper.core.dsl.expression.NthValue
import org.komapper.core.dsl.expression.Ntile
import org.komapper.core.dsl.expression.NullLiteralExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.PercentRank
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.expression.Rank
import org.komapper.core.dsl.expression.ReadOnlyColumnExpression
import org.komapper.core.dsl.expression.RowNumber
import org.komapper.core.dsl.expression.ScalarAliasExpression
import org.komapper.core.dsl.expression.ScalarArithmeticExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.ScalarQueryExpression
import org.komapper.core.dsl.expression.SqlBuilderScope
import org.komapper.core.dsl.expression.SqlBuilderScopeImpl
import org.komapper.core.dsl.expression.StringFunction
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.UserDefinedExpression
import org.komapper.core.dsl.expression.WindowDefinition
import org.komapper.core.dsl.expression.WindowFrame
import org.komapper.core.dsl.expression.WindowFrameBound
import org.komapper.core.dsl.expression.WindowFrameExclusion
import org.komapper.core.dsl.expression.WindowFrameKind
import org.komapper.core.dsl.expression.WindowFunction
import kotlin.reflect.typeOf

class BuilderSupport(
    private val dialect: BuilderDialect,
    private val aliasManager: AliasManager,
    private val buf: StatementBuffer,
    private val escapeSequence: String? = null,
) {
    private val operation = CriterionOperation()

    fun visitTableExpression(expression: TableExpression<*>, nameType: TableNameType) {
        val name = expression.getCanonicalTableName(dialect::enquote)
        when (nameType) {
            TableNameType.NAME_ONLY -> {
                buf.append(name)
            }

            TableNameType.ALIAS_ONLY -> {
                val alias = aliasManager.getAlias(expression)
                if (alias == null) {
                    buf.append(name)
                } else {
                    buf.append(alias)
                }
            }

            TableNameType.NAME_AND_ALIAS -> {
                buf.append(name)
                val alias = aliasManager.getAlias(expression)
                if (!alias.isNullOrBlank()) {
                    if (dialect.supportsAsKeywordForTableAlias()) {
                        buf.append(" as")
                    }
                    buf.append(" $alias")
                }
            }
        }
    }

    fun visitColumnExpression(expression: ColumnExpression<*, *>) {
        when (expression) {
            is AliasExpression<*, *> -> {
                visitAliasExpression(expression)
            }

            is ArithmeticExpression<*, *> -> {
                visitArithmeticExpression(expression)
            }

            is ConditionalExpression<*, *> -> {
                visitConditionalExpression(expression)
            }

            is DistinctExpression<*, *> -> {
                visitDistinctExpression(expression)
            }

            is LiteralExpression<*, *> -> {
                visitLiteralExpression(expression)
            }

            is MathematicalFunction<*, *> -> {
                visitMathematicalFunction(expression)
            }

            is ScalarExpression<*, *> -> {
                visitScalarExpression(expression)
            }

            is StringFunction -> {
                visitStringFunction(expression)
            }

            is WindowFunction<*, *> -> {
                visitWindowFunction(expression)
            }

            is WindowDefinition<*, *> -> {
                visitWindowDefinition(expression)
            }

            is UserDefinedExpression -> {
                visitUserDefinedExpression(expression)
            }

            is ReadOnlyColumnExpression<*, *>,
            is PropertyExpression<*, *>,
            -> {
                val name = expression.getCanonicalColumnName(dialect::enquote)
                val owner = expression.owner
                val alias = aliasManager.getAlias(owner)
                if (alias == null) {
                    val tableName = owner.getCanonicalTableName(dialect::enquote)
                    buf.append("$tableName.$name")
                } else if (alias.isBlank()) {
                    buf.append(name)
                } else {
                    if (alias == "excluded" && !dialect.supportsExcludedTable()) {
                        buf.append("values($name)")
                    } else {
                        buf.append("$alias.$name")
                    }
                }
            }
        }
    }

    private fun visitAliasExpression(expression: AliasExpression<*, *>) {
        visitColumnExpression(expression.expression)
        val alias = expression.alias.let { if (expression.alwaysQuoteAlias) dialect.enquote(it) else it }
        buf.append(" as $alias")
    }

    private fun visitArithmeticExpression(expression: ArithmeticExpression<*, *>) {
        buf.append("(")
        when (expression) {
            is ArithmeticExpression.Plus<*, *> -> {
                visitOperand(expression.left)
                buf.append(" + ")
                visitOperand(expression.right)
            }

            is ArithmeticExpression.Minus<*, *> -> {
                visitOperand(expression.left)
                buf.append(" - ")
                visitOperand(expression.right)
            }

            is ArithmeticExpression.Times<*, *> -> {
                visitOperand(expression.left)
                buf.append(" * ")
                visitOperand(expression.right)
            }

            is ArithmeticExpression.Div<*, *> -> {
                visitOperand(expression.left)
                buf.append(" / ")
                visitOperand(expression.right)
            }

            is ArithmeticExpression.Mod<*, *> -> {
                if (dialect.supportsModuloOperator()) {
                    visitOperand(expression.left)
                    buf.append(" % ")
                    visitOperand(expression.right)
                } else {
                    buf.append("mod(")
                    visitOperand(expression.left)
                    buf.append(", ")
                    visitOperand(expression.right)
                    buf.append(")")
                }
            }
        }
        buf.append(")")
    }

    private fun visitConditionalExpression(expression: ConditionalExpression<*, *>) {
        when (expression) {
            is ConditionalExpression.Case<*, *> -> {
                visitCaseExpression(expression)
            }

            is ConditionalExpression.Coalesce<*, *> -> {
                visitCoalesceExpression(expression)
            }
        }
    }

    private fun visitCaseExpression(expression: ConditionalExpression.Case<*, *>) {
        buf.append("case")
        for (`when` in expression.whenList) {
            if (`when`.criteria.isNotEmpty()) {
                buf.append(" when ")
                for ((index, criterion) in `when`.criteria.withIndex()) {
                    visitCriterion(index, criterion)
                    buf.append(" and ")
                }
                buf.cutBack(5)
                buf.append(" then ")
                visitOperand(`when`.thenOperand)
            }
        }
        if (expression.otherwise != null) {
            buf.append(" else ")
            visitColumnExpression(expression.otherwise)
        }
        buf.append(" end")
    }

    private fun visitCoalesceExpression(expression: ConditionalExpression.Coalesce<*, *>) {
        buf.append("coalesce(")
        visitColumnExpression(expression.first)
        buf.append(", ")
        visitColumnExpression(expression.second)
        for (e in expression.expressions) {
            buf.append(", ")
            visitColumnExpression(e)
        }
        buf.append(")")
    }

    private fun visitDistinctExpression(expression: DistinctExpression<*, *>) {
        buf.append("distinct ")
        visitColumnExpression(expression.expression)
    }

    private fun visitLiteralExpression(expression: LiteralExpression<*, *>) {
        val literal = when (expression) {
            is NullLiteralExpression -> "null"
            is NonNullLiteralExpression -> dialect.formatValue(expression.value, expression.interiorType, false)
        }
        buf.append(literal)
    }

    private fun visitMathematicalFunction(function: MathematicalFunction<*, *>) {
        @Suppress("USELESS_IS_CHECK")
        when (function) {
            is MathematicalFunction<*, *> -> {
                val random = dialect.getRandomFunction()
                buf.append("$random()")
            }
        }
    }

    private fun visitScalarExpression(expression: ScalarExpression<*, *>) {
        when (expression) {
            is AggregateFunction<*, *> -> {
                visitAggregateFunction(expression)
            }

            is ScalarAliasExpression -> {
                visitAliasExpression(expression.expression)
            }

            is ScalarArithmeticExpression -> {
                visitArithmeticExpression(expression.expression)
            }

            is ScalarQueryExpression<*, *, *> -> {
                visitScalarQueryExpression(expression)
            }
        }
    }

    private fun visitAggregateFunction(function: AggregateFunction<*, *>) {
        when (function) {
            is AggregateFunction.Avg -> {
                buf.append("avg(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }

            is AggregateFunction.CountAsterisk -> {
                buf.append("count(*)")
            }

            is AggregateFunction.Count -> {
                buf.append("count(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }

            is AggregateFunction.Max<*, *> -> {
                buf.append("max(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }

            is AggregateFunction.Min<*, *> -> {
                buf.append("min(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }

            is AggregateFunction.Sum<*, *> -> {
                buf.append("sum(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }
        }
    }

    private fun visitScalarQueryExpression(expression: ScalarQueryExpression<*, *, *>) {
        buf.append("(")
        val statement = buildSubqueryStatement(expression.context)
        buf.append(statement)
        buf.append(")")
    }

    fun buildSubqueryStatement(
        context: SubqueryContext,
        projectionPredicate: (ColumnExpression<*, *>) -> Boolean = { true },
    ): Statement {
        return when (context) {
            is SelectContext<*, *, *> -> {
                val childAliasManager = DefaultAliasManager(context, aliasManager)
                val builder = SelectStatementBuilder(dialect, context, childAliasManager, projectionPredicate)
                builder.build()
            }

            is SetOperationContext -> {
                val builder = SetOperationStatementBuilder(dialect, context, aliasManager, projectionPredicate)
                builder.build()
            }
        }
    }

    private fun visitStringFunction(function: StringFunction<*, *>) {
        buf.append("(")
        when (function) {
            is StringFunction.Concat -> {
                buf.append("concat(")
                visitOperand(function.left)
                buf.append(", ")
                visitOperand(function.right)
                buf.append(")")
            }

            is StringFunction.Locate -> {
                when (val type = dialect.getLocateFunctionType()) {
                    LocateFunctionType.LOCATE,
                    LocateFunctionType.CHARINDEX,
                    -> {
                        buf.append("${type.functionName}(")
                        visitOperand(function.pattern)
                        buf.append(", ")
                        visitOperand(function.string)
                        if (function.startIndex != null) {
                            buf.append(", ")
                            visitOperand(function.startIndex)
                        }
                        buf.append(")")
                    }

                    LocateFunctionType.INSTR -> {
                        buf.append("${type.functionName}(")
                        visitOperand(function.string)
                        buf.append(", ")
                        visitOperand(function.pattern)
                        if (function.startIndex != null) {
                            buf.append(", ")
                            visitOperand(function.startIndex)
                        }
                        buf.append(")")
                    }

                    LocateFunctionType.POSITION -> {
                        if (function.startIndex == null) {
                            buf.append("${type.functionName}(")
                            visitOperand(function.pattern)
                            buf.append(" in ")
                            visitOperand(function.string)
                            buf.append(")")
                        } else {
                            buf.append("${type.functionName}(")
                            visitOperand(function.pattern)
                            buf.append(" in substring(")
                            visitOperand(function.string)
                            buf.append(" from ")
                            visitOperand(function.startIndex)
                            buf.append(")) + ")
                            visitOperand(function.startIndex)
                            buf.append(" - 1")
                        }
                    }
                }
            }

            is StringFunction.Lower -> {
                buf.append("lower(")
                visitOperand(function.operand)
                buf.append(")")
            }

            is StringFunction.Ltrim -> {
                buf.append("ltrim(")
                visitOperand(function.operand)
                buf.append(")")
            }

            is StringFunction.Rtrim -> {
                buf.append("rtrim(")
                visitOperand(function.operand)
                buf.append(")")
            }

            is StringFunction.Substring -> {
                val substring = dialect.getSubstringFunction()
                buf.append("$substring(")
                visitOperand(function.target)
                buf.append(", ")
                visitOperand(function.startIndex)
                val length = function.length
                if (length == null) {
                    val defaultLength = dialect.getDefaultLengthForSubstringFunction()
                    if (defaultLength != null) {
                        buf.append(", $defaultLength")
                    }
                } else {
                    buf.append(", ")
                    visitOperand(length)
                }
                buf.append(")")
            }

            is StringFunction.Trim -> {
                buf.append("trim(")
                visitOperand(function.operand)
                buf.append(")")
            }

            is StringFunction.Upper -> {
                buf.append("upper(")
                visitOperand(function.operand)
                buf.append(")")
            }
        }
        buf.append(")")
    }

    private fun visitWindowDefinition(definition: WindowDefinition<*, *>) {
        visitWindowFunction(definition.function)
        buf.append(" over(")
        if (definition.partitionBy.isNotEmpty()) {
            buf.append("partition by ")
            for (expression in definition.partitionBy) {
                visitColumnExpression(expression)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        if (definition.orderBy.isNotEmpty()) {
            val orderBySupport = OrderByBuilderSupport(dialect, definition.orderBy, aliasManager, buf)
            orderBySupport.orderByClause()
        }
        val frame = definition.frame
        if (frame != null) {
            visitWindowFrame(frame)
        }
        buf.append(")")
    }

    private fun visitWindowFunction(function: WindowFunction<*, *>) {
        when (function) {
            is RowNumber -> buf.append("row_number()")
            is Rank -> buf.append("rank()")
            is DenseRank -> buf.append("dense_rank()")
            is PercentRank -> buf.append("percent_rank()")
            is CumeDist -> buf.append("cume_dist()")
            is Ntile -> {
                buf.append("ntile(")
                visitOperand(function.bucketSize)
                buf.append(")")
            }

            is Lead -> {
                buf.append("lead(")
                visitColumnExpression(function.expression)
                val offset = function.offset
                if (offset != null) {
                    buf.append(", ")
                    visitOperand(offset)
                }
                val default = function.default
                if (default != null) {
                    buf.append(", ")
                    visitOperand(default)
                }
                buf.append(")")
            }

            is Lag -> {
                buf.append("lag(")
                visitColumnExpression(function.expression)
                val offset = function.offset
                if (offset != null) {
                    buf.append(", ")
                    visitOperand(offset)
                }
                val default = function.default
                if (default != null) {
                    buf.append(", ")
                    visitOperand(default)
                }
                buf.append(")")
            }

            is FirstValue -> {
                buf.append("first_value(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }

            is LastValue -> {
                buf.append("last_value(")
                visitColumnExpression(function.expression)
                buf.append(")")
            }

            is NthValue -> {
                buf.append("nth_value(")
                visitColumnExpression(function.expression)
                buf.append(", ")
                visitOperand(function.offset)
                buf.append(")")
            }

            is AggregateFunction<*, *> -> visitAggregateFunction(function)
        }
    }

    private fun visitWindowFrame(frame: WindowFrame) {
        buf.append(" ")
        when (frame.kind) {
            WindowFrameKind.GROUPS -> buf.append("groups")
            WindowFrameKind.RANGE -> buf.append("range")
            WindowFrameKind.ROWS -> buf.append("rows")
        }
        buf.append(" ")
        if (frame.end == null) {
            visitWindowFrameBound(frame.start)
        } else {
            buf.append("between ")
            visitWindowFrameBound(frame.start)
            buf.append(" and ")
            visitWindowFrameBound(frame.end)
        }
        if (frame.exclusion != null) {
            buf.append(" ")
            visitWindowFrameExclusion(frame.exclusion)
        }
    }

    private fun visitWindowFrameBound(bound: WindowFrameBound) {
        when (bound) {
            is WindowFrameBound.CurrentRow -> buf.append("current row")
            is WindowFrameBound.UnboundedFollowing -> buf.append("unbounded following")
            is WindowFrameBound.UnboundedPreceding -> buf.append("unbounded preceding")
            is WindowFrameBound.Following -> {
                if (dialect.supportsParameterBindingForWindowFrameBoundOffset()) {
                    buf.bind(Value(bound.offset, typeOf<Int>()))
                } else {
                    buf.append(bound.offset.toString())
                }
                buf.append(" following")
            }

            is WindowFrameBound.Preceding -> {
                if (dialect.supportsParameterBindingForWindowFrameBoundOffset()) {
                    buf.bind(Value(bound.offset, typeOf<Int>()))
                } else {
                    buf.append(bound.offset.toString())
                }
                buf.append(" preceding")
            }
        }
    }

    private fun visitWindowFrameExclusion(exclusion: WindowFrameExclusion) {
        buf.append("exclude ")
        when (exclusion) {
            is WindowFrameExclusion.CurrentRow -> buf.append("current row")
            is WindowFrameExclusion.Group -> buf.append("group")
            is WindowFrameExclusion.NoOthers -> buf.append("no others")
            is WindowFrameExclusion.Ties -> buf.append("ties")
        }
    }

    private fun visitUserDefinedExpression(expression: UserDefinedExpression<*, *>) {
        buf.append("(")
        val scope = SqlBuilderScopeImpl(dialect, buf, ::visitOperand)
        expression.build(scope)
        buf.append(")")
    }

    private fun buildEscapedValuePair(
        escapeExpression: EscapeExpression,
        masking: Boolean,
    ): Pair<Value<String>, Value<String>> {
        val patternBuf = StringBuilder(escapeExpression.length + 10)
        val finalEscapeSequence = escapeSequence ?: dialect.escapeSequence
        fun visit(e: EscapeExpression) {
            when (e) {
                is EscapeExpression.Text -> patternBuf.append(e.value)
                is EscapeExpression.Escape -> {
                    val escaped = dialect.escape(e.value.toString(), finalEscapeSequence)
                    patternBuf.append(escaped)
                }

                is EscapeExpression.Composite -> {
                    visit(e.left)
                    visit(e.right)
                }
            }
        }
        visit(escapeExpression)
        val first = Value(patternBuf.toString(), typeOf<String>(), masking)
        val second = Value(finalEscapeSequence, typeOf<String>())
        return first to second
    }

    fun visitOperand(operand: Operand) {
        when (operand) {
            is Operand.Column -> {
                visitColumnExpression(operand.expression)
            }

            is Operand.Argument<*, *> -> {
                buf.bind(operand.value)
            }

            is Operand.SimpleArgument<*> -> {
                buf.bind(operand.value)
            }

            is Operand.Escape -> {
                val values = buildEscapedValuePair(operand.escapeExpression, operand.masking)
                buf.bind(values.first)
                buf.append(" escape ")
                buf.bind(values.second)
            }

            is Operand.Subquery -> {
                val statement = buildSubqueryStatement(operand.subqueryExpression.context)
                buf.append(statement)
            }
        }
    }

    fun visitCriterion(index: Int, c: Criterion) {
        when (c) {
            is Criterion.Eq -> operation.binary(c.left, c.right, "=")
            is Criterion.NotEq -> operation.binary(c.left, c.right, "<>")
            is Criterion.Less -> operation.binary(c.left, c.right, "<")
            is Criterion.LessEq -> operation.binary(c.left, c.right, "<=")
            is Criterion.Greater -> operation.binary(c.left, c.right, ">")
            is Criterion.GreaterEq -> operation.binary(c.left, c.right, ">=")
            is Criterion.IsNull -> operation.isNull(c.left)
            is Criterion.IsNotNull -> operation.isNull(c.left, true)
            is Criterion.Like -> operation.like(c.left, c.right)
            is Criterion.NotLike -> operation.like(c.left, c.right, true)
            is Criterion.Between -> operation.between(c.left, c.right)
            is Criterion.NotBetween -> operation.between(c.left, c.right, true)
            is Criterion.InList -> operation.inList(c.left, c.right)
            is Criterion.NotInList -> operation.inList(c.left, c.right, true)
            is Criterion.InList2 -> operation.inList2(c.left, c.right)
            is Criterion.NotInList2 -> operation.inList2(c.left, c.right, true)
            is Criterion.InSubQuery -> operation.inSubQuery(c.left, c.right)
            is Criterion.NotInSubQuery -> operation.inSubQuery(c.left, c.right, true)
            is Criterion.InSubQuery2 -> operation.inSubQuery2(c.left, c.right)
            is Criterion.NotInSubQuery2 -> operation.inSubQuery2(c.left, c.right, true)
            is Criterion.Exists -> operation.exists(c.operand)
            is Criterion.NotExists -> operation.exists(c.operand, true)
            is Criterion.And -> operation.logicalBinary("and", c.criteria, index)
            is Criterion.Or -> operation.logicalBinary("or", c.criteria, index)
            is Criterion.Not -> operation.not(c.criteria)
            is Criterion.UserDefined -> operation.userDefined(c.build)
        }
    }

    private inner class CriterionOperation {
        fun binary(left: Operand, right: Operand, operator: String) {
            visitOperand(left)
            buf.append(" $operator ")
            visitOperand(right)
        }

        fun isNull(left: Operand, not: Boolean = false) {
            visitOperand(left)
            val predicate = if (not) {
                " is not null"
            } else {
                " is null"
            }
            buf.append(predicate)
        }

        fun like(left: Operand, right: Operand, not: Boolean = false) {
            visitOperand(left)
            if (not) {
                buf.append(" not")
            }
            buf.append(" like ")
            visitOperand(right)
        }

        fun between(left: Operand, right: Pair<Operand, Operand>, not: Boolean = false) {
            visitOperand(left)
            if (not) {
                buf.append(" not")
            }
            buf.append(" between ")
            val (start, end) = right
            visitOperand(start)
            buf.append(" and ")
            visitOperand(end)
        }

        fun inList(left: Operand, right: List<Operand>, not: Boolean = false) {
            visitOperand(left)
            if (not) {
                buf.append(" not")
            }
            buf.append(" in (")
            if (right.isEmpty()) {
                buf.append("null")
            } else {
                for (parameter in right) {
                    visitOperand(parameter)
                    buf.append(", ")
                }
                buf.cutBack(2)
            }
            buf.append(")")
        }

        fun inList2(
            left: Pair<Operand, Operand>,
            right: List<Pair<Operand, Operand>>,
            not: Boolean = false,
        ) {
            if (dialect.supportsMultipleColumnsInInPredicate()) {
                buf.append("(")
                visitOperand(left.first)
                buf.append(", ")
                visitOperand(left.second)
                buf.append(")")
                if (not) {
                    buf.append(" not")
                }
                buf.append(" in (")
                if (right.isEmpty()) {
                    buf.append("(null, null)")
                } else {
                    for ((first, second) in right) {
                        buf.append("(")
                        visitOperand(first)
                        buf.append(", ")
                        visitOperand(second)
                        buf.append(")")
                        buf.append(", ")
                    }
                    buf.cutBack(2)
                }
                buf.append(")")
            } else {
                if (not) {
                    buf.append("not ")
                }
                buf.append("(")
                if (right.isEmpty()) {
                    visitOperand(left.first)
                    buf.append(" = null")
                    buf.append(" and ")
                    visitOperand(left.second)
                    buf.append(" = null")
                } else {
                    for ((first, second) in right) {
                        buf.append("(")
                        visitOperand(left.first)
                        buf.append(" = ")
                        visitOperand(first)
                        buf.append(" and ")
                        visitOperand(left.second)
                        buf.append(" = ")
                        visitOperand(second)
                        buf.append(")")
                        buf.append(" or ")
                    }
                    buf.cutBack(4)
                }
                buf.append(")")
            }
        }

        fun inSubQuery(left: Operand, right: Operand, not: Boolean = false) {
            visitOperand(left)
            if (not) {
                buf.append(" not")
            }
            buf.append(" in (")
            visitOperand(right)
            buf.append(")")
        }

        fun inSubQuery2(left: Pair<Operand, Operand>, right: Operand, not: Boolean = false) {
            if (!dialect.supportsMultipleColumnsInInPredicate()) {
                throw UnsupportedOperationException("Dialect(driver=${dialect.driver}) does not support multiple columns in IN predicate.")
            }
            buf.append("(")
            visitOperand(left.first)
            buf.append(", ")
            visitOperand(left.second)
            buf.append(")")
            if (not) {
                buf.append(" not")
            }
            buf.append(" in (")
            visitOperand(right)
            buf.append(")")
        }

        fun exists(operand: Operand, not: Boolean = false) {
            if (not) {
                buf.append("not ")
            }
            buf.append("exists (")
            visitOperand(operand)
            buf.append(")")
        }

        fun logicalBinary(operator: String, criteria: List<Criterion>, index: Int) {
            if (criteria.isNotEmpty()) {
                if (index > 0) {
                    buf.cutBack(5)
                }
                if (index != 0) {
                    buf.append(" $operator ")
                }
                buf.append("(")
                for ((i, c) in criteria.withIndex()) {
                    visitCriterion(i, c)
                    buf.append(" and ")
                }
                buf.cutBack(5)
                buf.append(")")
            }
        }

        fun not(criteria: List<Criterion>) {
            if (criteria.isNotEmpty()) {
                buf.append("not ")
                buf.append("(")
                for ((index, c) in criteria.withIndex()) {
                    visitCriterion(index, c)
                    buf.append(" and ")
                }
                buf.cutBack(5)
                buf.append(")")
            }
        }

        fun userDefined(build: SqlBuilderScope.() -> Unit) {
            val scope = SqlBuilderScopeImpl(dialect, buf, ::visitOperand)
            scope.build()
        }
    }
}
