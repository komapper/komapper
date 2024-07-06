package org.komapper.core

import kotlin.reflect.KType

fun KType.mustNotBeMarkedNullable(className: String?, propertyName: String) {
    if (this.isMarkedNullable) {
        error("The '$propertyName' property of '$className' must not be marked as nullable: $this")
    }
}
