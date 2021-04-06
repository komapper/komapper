package org.komapper.core.dsl.expr

import org.komapper.core.dsl.metamodel.EntityMetamodel
import kotlin.reflect.KClass

internal sealed class IndexedSortItem : Number(), PropertyExpression<Number> {
    abstract val index: Number

    override val owner: EntityMetamodel<*>
        get() = throw UnsupportedOperationException()
    override val klass: KClass<Number>
        get() = throw UnsupportedOperationException()
    override val columnName: String
        get() = throw UnsupportedOperationException()

    override fun getCanonicalColumnName(mapper: (String) -> String): String {
        return index.toString()
    }

    override fun toByte(): Byte {
        return index.toByte()
    }

    override fun toChar(): Char {
        return index.toChar()
    }

    override fun toDouble(): Double {
        return index.toDouble()
    }

    override fun toFloat(): Float {
        return index.toFloat()
    }

    override fun toInt(): Int {
        return index.toInt()
    }

    override fun toLong(): Long {
        return index.toLong()
    }

    override fun toShort(): Short {
        return index.toShort()
    }

    data class Asc(override val index: Number) : IndexedSortItem()
    data class Desc(override val index: Number) : IndexedSortItem()
}
