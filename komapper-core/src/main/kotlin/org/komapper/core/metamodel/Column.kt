package org.komapper.core.metamodel

import kotlin.reflect.KClass

interface Column<T : Any> {
    val owner: Table
    val klass: KClass<T>
    val columnName: String
}
