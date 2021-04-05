package org.komapper.core.dsl.element

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.TableInfo
import kotlin.reflect.KClass

internal sealed class SortIndex : Number(), ColumnInfo<Number> {
    abstract val index: Number

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

    override val owner: TableInfo
        get() = throw UnsupportedOperationException()
    override val klass: KClass<Number>
        get() = throw UnsupportedOperationException()
    override val columnName: String
        get() = throw UnsupportedOperationException()

    data class Asc(override val index: Number) : SortIndex()
    data class Desc(override val index: Number) : SortIndex()
}
