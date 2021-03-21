package org.komapper.core.query.data

import org.komapper.core.metamodel.PropertyMetamodel

internal sealed class SortItem<E, T : Any> : PropertyMetamodel<E, T> {
    abstract val propertyMetamodel: PropertyMetamodel<E, T>

    data class Asc<E, T : Any>(override val propertyMetamodel: PropertyMetamodel<E, T>) :
        SortItem<E, T>(),
        PropertyMetamodel<E, T> by propertyMetamodel

    data class Desc<E, T : Any>(override val propertyMetamodel: PropertyMetamodel<E, T>) :
        SortItem<E, T>(),
        PropertyMetamodel<E, T> by propertyMetamodel
}
