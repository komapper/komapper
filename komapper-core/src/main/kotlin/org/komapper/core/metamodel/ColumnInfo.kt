package org.komapper.core.metamodel

import kotlin.reflect.KClass

interface ColumnInfo<T : Any> {
    val owner: TableInfo
    val klass: KClass<T>
    val columnName: String
}
