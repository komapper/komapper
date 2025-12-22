package org.komapper.spring.boot.test.autoconfigure.jdbc

import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.boot.test.context.filter.annotation.StandardAnnotationCustomizableTypeExcludeFilter

/**
 * [TypeExcludeFilter] for [@KomapperJdbcTest][KomapperJdbcTest].
 */
internal class KomapperJdbcTypeExcludeFilter(testClass: Class<*>) :
    StandardAnnotationCustomizableTypeExcludeFilter<KomapperJdbcTest>(testClass)
