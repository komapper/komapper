package org.komapper.spring.boot.test.autoconfigure.jdbc

import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.boot.test.autoconfigure.filter.StandardAnnotationCustomizableTypeExcludeFilter

/**
 * [TypeExcludeFilter] for [@KomapperJdbcTest][KomapperJdbcTest].
 */
internal class KomapperJdbcTypeExcludeFilter(testClass: Class<*>) :
    StandardAnnotationCustomizableTypeExcludeFilter<KomapperJdbcTest>(testClass)
