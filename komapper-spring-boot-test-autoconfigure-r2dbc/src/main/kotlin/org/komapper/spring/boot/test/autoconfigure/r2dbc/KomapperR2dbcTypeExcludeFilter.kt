package org.komapper.spring.boot.test.autoconfigure.r2dbc

import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.boot.test.context.filter.annotation.StandardAnnotationCustomizableTypeExcludeFilter

/**
 * [TypeExcludeFilter] for [@KomapperR2dbcTest][KomapperR2dbcTest].
 */
internal class KomapperR2dbcTypeExcludeFilter(testClass: Class<*>) :
    StandardAnnotationCustomizableTypeExcludeFilter<KomapperR2dbcTest>(testClass)
