package org.komapper.tx.core

import org.komapper.core.ThreadSafe
import java.sql.Connection
import java.time.Duration

@ThreadSafe
interface TransactionProperty {

    operator fun <E : Element<*>> get(key: Key<E>): E?

    fun <R> fold(initial: R, operation: (R, Element<*>) -> R): R

    infix operator fun plus(other: TransactionProperty): TransactionProperty {
        return if (other === EmptyTransactionProperty) {
            this
        } else {
            other.fold(this) { acc, element ->
                val removed = acc.minusKey(element.key)
                if (removed === EmptyTransactionProperty) {
                    element
                } else {
                    CombinedTransactionOption(removed, element)
                }
            }
        }
    }

    fun minusKey(key: Key<*>): TransactionProperty

    interface Key<E : Element<*>>

    interface Element<T> : TransactionProperty {
        val key: Key<*>
        val value: T

        override operator fun <E : Element<*>> get(key: Key<E>): E? {
            @Suppress("UNCHECKED_CAST")
            return if (this.key == key) this as E else null
        }

        override fun <R> fold(initial: R, operation: (R, Element<*>) -> R): R =
            operation(initial, this)

        override fun minusKey(key: Key<*>): TransactionProperty =
            if (this.key == key) EmptyTransactionProperty else this
    }

    enum class IsolationLevel(override val value: Int) : Element<Int> {
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

        companion object Key : TransactionProperty.Key<IsolationLevel>

        override val key: TransactionProperty.Key<IsolationLevel> get() = Key

        override fun toString(): String {
            return "IsolationLevel($name)"
        }
    }

    data class Name(override val value: String) : Element<String> {
        companion object Key : TransactionProperty.Key<Name>

        override val key: TransactionProperty.Key<Name> = Key

        override fun toString(): String {
            return "Name($value)"
        }
    }

    data class ReadOnly(override val value: Boolean) : Element<Boolean> {
        companion object Key : TransactionProperty.Key<ReadOnly>

        override val key: TransactionProperty.Key<ReadOnly> = Key

        override fun toString(): String {
            return "ReadOnly($value)"
        }
    }

    data class LockWaitTime(override val value: Duration) : Element<Duration> {
        companion object Key : TransactionProperty.Key<LockWaitTime>

        override val key: TransactionProperty.Key<LockWaitTime> = Key

        override fun toString(): String {
            return "LockWaitTime($value)"
        }
    }
}

private data class CombinedTransactionOption(
    val left: TransactionProperty,
    val element: TransactionProperty.Element<*>
) : TransactionProperty {

    override fun <E : TransactionProperty.Element<*>> get(key: TransactionProperty.Key<E>): E? {
        var cur = this
        while (true) {
            cur.element[key]?.let { return it }
            val next = cur.left
            if (next is CombinedTransactionOption) {
                cur = next
            } else {
                return next[key]
            }
        }
    }

    override fun <R> fold(initial: R, operation: (R, TransactionProperty.Element<*>) -> R): R =
        operation(left.fold(initial, operation), element)

    override fun minusKey(key: TransactionProperty.Key<*>): TransactionProperty {
        element[key]?.let { return left }
        val newLeft = left.minusKey(key)
        return when {
            newLeft === left -> this
            newLeft === EmptyTransactionProperty -> element
            else -> CombinedTransactionOption(newLeft, element)
        }
    }

    override fun toString(): String =
        "[" + fold("") { acc, element ->
            if (acc.isEmpty()) element.toString() else "$acc, $element"
        } + "]"
}

object EmptyTransactionProperty : TransactionProperty {
    override fun <E : TransactionProperty.Element<*>> get(key: TransactionProperty.Key<E>): E? = null
    override fun <R> fold(initial: R, operation: (R, TransactionProperty.Element<*>) -> R): R = initial
    override fun minusKey(key: TransactionProperty.Key<*>): TransactionProperty = this
    override fun toString(): String = "EmptyTransactionProperty"
}
