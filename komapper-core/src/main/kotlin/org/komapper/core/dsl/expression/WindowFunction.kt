@file:Suppress("ConvertObjectToDataObject")

package org.komapper.core.dsl.expression

import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed interface WindowFunction<T : Any, S : Any> : ColumnExpression<T, S>

internal object RowNumber : WindowFunction<Long, Long> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorType: KType = typeOf<Long>()
    override val interiorType: KType = typeOf<Long>()
    override val wrap: (Long) -> Long = { it }
    override val unwrap: (Long) -> Long = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object Rank : WindowFunction<Long, Long> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorType: KType = typeOf<Long>()
    override val interiorType: KType = typeOf<Long>()
    override val wrap: (Long) -> Long = { it }
    override val unwrap: (Long) -> Long = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object DenseRank : WindowFunction<Long, Long> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorType: KType = typeOf<Long>()
    override val interiorType: KType = typeOf<Long>()
    override val wrap: (Long) -> Long = { it }
    override val unwrap: (Long) -> Long = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object PercentRank : WindowFunction<Double, Double> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorType: KType = typeOf<Double>()
    override val interiorType: KType = typeOf<Double>()
    override val wrap: (Double) -> Double = { it }
    override val unwrap: (Double) -> Double = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object CumeDist : WindowFunction<Double, Double> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorType: KType = typeOf<Double>()
    override val interiorType: KType = typeOf<Double>()
    override val wrap: (Double) -> Double = { it }
    override val unwrap: (Double) -> Double = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal data class Ntile(val bucketSize: Operand) : WindowFunction<Int, Int> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorType: KType = typeOf<Int>()
    override val interiorType: KType = typeOf<Int>()
    override val wrap: (Int) -> Int = { it }
    override val unwrap: (Int) -> Int = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal data class Lead<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
    val offset: Operand?,
    val default: Operand?,
) : WindowFunction<T, S>, ColumnExpression<T, S> by expression

internal data class Lag<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
    val offset: Operand?,
    val default: Operand?,
) : WindowFunction<T, S>, ColumnExpression<T, S> by expression

internal data class FirstValue<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
) : WindowFunction<T, S>, ColumnExpression<T, S> by expression

internal data class LastValue<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
) : WindowFunction<T, S>, ColumnExpression<T, S> by expression

internal data class NthValue<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
    val offset: Operand,
) : WindowFunction<T, S>, ColumnExpression<T, S> by expression

interface WindowDefinition<T : Any, S : Any> : ColumnExpression<T, S> {
    val function: WindowFunction<T, S>
    val partitionBy: List<ColumnExpression<*, *>>
    val orderBy: List<SortItem>
    val frame: WindowFrame?
}

internal data class WindowDefinitionImpl<T : Any, S : Any>(
    override val function: WindowFunction<T, S>,
    override val partitionBy: List<ColumnExpression<*, *>>,
    override val orderBy: List<SortItem>,
    override val frame: WindowFrame?,
) : WindowDefinition<T, S> {
    override val owner: TableExpression<*> get() = function.owner
    override val exteriorType: KType = function.exteriorType
    override val interiorType: KType = function.interiorType
    override val wrap: (S) -> T get() = function.wrap
    override val unwrap: (T) -> S get() = function.unwrap
    override val columnName: String get() = function.columnName
    override val alwaysQuote: Boolean get() = function.alwaysQuote
    override val masking: Boolean get() = function.masking
}

data class WindowFrame(
    val kind: WindowFrameKind,
    val start: WindowFrameBound,
    val end: WindowFrameBound?,
    val exclusion: WindowFrameExclusion?,
)

enum class WindowFrameKind {
    GROUPS,
    RANGE,
    ROWS,
}

sealed interface WindowFrameBound {
    object CurrentRow : WindowFrameBound
    object UnboundedPreceding : WindowFrameBound
    object UnboundedFollowing : WindowFrameBound
    data class Preceding(val offset: Int) : WindowFrameBound
    data class Following(val offset: Int) : WindowFrameBound
}

sealed interface WindowFrameExclusion {
    object CurrentRow : WindowFrameExclusion
    object Group : WindowFrameExclusion
    object Ties : WindowFrameExclusion
    object NoOthers : WindowFrameExclusion
}
