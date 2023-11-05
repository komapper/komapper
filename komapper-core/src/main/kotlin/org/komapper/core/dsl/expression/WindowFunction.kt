@file:Suppress("ConvertObjectToDataObject")

package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

sealed interface WindowFunction<T : Any, S : Any> : ColumnExpression<T, S>

internal object RowNumber : WindowFunction<Long, Long> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<Long> = Long::class
    override val interiorClass: KClass<Long> = Long::class
    override val wrap: (Long) -> Long = { it }
    override val unwrap: (Long) -> Long = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object Rank : WindowFunction<Long, Long> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<Long> = Long::class
    override val interiorClass: KClass<Long> = Long::class
    override val wrap: (Long) -> Long = { it }
    override val unwrap: (Long) -> Long = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object DenseRank : WindowFunction<Long, Long> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<Long> = Long::class
    override val interiorClass: KClass<Long> = Long::class
    override val wrap: (Long) -> Long = { it }
    override val unwrap: (Long) -> Long = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object PercentRank : WindowFunction<Double, Double> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<Double> = Double::class
    override val interiorClass: KClass<Double> = Double::class
    override val wrap: (Double) -> Double = { it }
    override val unwrap: (Double) -> Double = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal object CumeDist : WindowFunction<Double, Double> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<Double> = Double::class
    override val interiorClass: KClass<Double> = Double::class
    override val wrap: (Double) -> Double = { it }
    override val unwrap: (Double) -> Double = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

internal data class Ntile(val bucketSize: Operand) : WindowFunction<Int, Int> {
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<Int> = Int::class
    override val interiorClass: KClass<Int> = Int::class
    override val wrap: (Int) -> Int = { it }
    override val unwrap: (Int) -> Int = { it }
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
}

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
    override val exteriorClass: KClass<T> get() = function.exteriorClass
    override val interiorClass: KClass<S> get() = function.interiorClass
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
    GROUPS, RANGE, ROWS,
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
