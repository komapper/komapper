package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.Option
import io.r2dbc.spi.TransactionDefinition
import org.komapper.tx.core.TransactionProperty
import java.time.Duration

fun <T> defineTransactionProperty(option: Option<T>, value: T): TransactionProperty {
    // handle well-known options
    val property = when (option) {
        TransactionDefinition.ISOLATION_LEVEL -> {
            when (value) {
                IsolationLevel.READ_UNCOMMITTED -> TransactionProperty.IsolationLevel.READ_UNCOMMITTED
                IsolationLevel.READ_COMMITTED -> TransactionProperty.IsolationLevel.READ_COMMITTED
                IsolationLevel.REPEATABLE_READ -> TransactionProperty.IsolationLevel.REPEATABLE_READ
                IsolationLevel.SERIALIZABLE -> TransactionProperty.IsolationLevel.SERIALIZABLE
                else -> null
            }
        }
        TransactionDefinition.LOCK_WAIT_TIMEOUT -> {
            when (value) {
                is Duration -> TransactionProperty.LockWaitTime(value)
                else -> null
            }
        }
        TransactionDefinition.NAME -> {
            when (value) {
                is String -> TransactionProperty.Name(value)
                else -> null
            }
        }
        TransactionDefinition.READ_ONLY -> {
            when (value) {
                is Boolean -> TransactionProperty.ReadOnly(value)
                else -> null
            }
        }
        else -> null
    }
    return property ?: OptionElement(option, value)
}

private class OptionElement<T>(private val option: Option<T>, override val value: T) : TransactionProperty.Element<T> {
    override val key: TransactionProperty.Key<OptionElement<T>> = OptionKey(option)
    override fun toString(): String {
        return "<$option($value)>"
    }
}

private data class OptionKey<T>(val option: Option<T>) :
    TransactionProperty.Key<OptionElement<T>> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OptionKey<*>
        if (option != other.option) return false
        return true
    }

    override fun hashCode(): Int {
        return option.hashCode()
    }
}

fun <T> Option<T>.asKey(): TransactionProperty.Key<*> {
    // handle well-known options
    val key = when (this) {
        TransactionDefinition.ISOLATION_LEVEL -> TransactionProperty.IsolationLevel
        TransactionDefinition.LOCK_WAIT_TIMEOUT -> TransactionProperty.LockWaitTime
        TransactionDefinition.NAME -> TransactionProperty.Name
        TransactionDefinition.READ_ONLY -> TransactionProperty.ReadOnly
        else -> null
    }
    return key ?: OptionKey(this)
}

fun TransactionProperty.asDefinition(): TransactionDefinition {
    return TransactionOptionAdapter(this)
}

private class TransactionOptionAdapter(val adaptee: TransactionProperty) : TransactionDefinition {
    override fun <T : Any> getAttribute(option: Option<T>): T? {
        val element = adaptee[option.asKey()]
        // handle well-known properties
        val value: Any? = when (element) {
            is TransactionProperty.IsolationLevel -> {
                when (element) {
                    TransactionProperty.IsolationLevel.READ_UNCOMMITTED -> IsolationLevel.READ_UNCOMMITTED
                    TransactionProperty.IsolationLevel.READ_COMMITTED -> IsolationLevel.READ_COMMITTED
                    TransactionProperty.IsolationLevel.REPEATABLE_READ -> IsolationLevel.REPEATABLE_READ
                    TransactionProperty.IsolationLevel.SERIALIZABLE -> IsolationLevel.SERIALIZABLE
                }
            }
            is TransactionProperty.LockWaitTime -> element.value
            is TransactionProperty.Name -> element.value
            is TransactionProperty.ReadOnly -> element.value
            else -> null
        }
        @Suppress("UNCHECKED_CAST")
        return (value ?: element?.value) as T?
    }
}
