package org.komapper.spring.boot.test.autoconfigure.jdbc

import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.test.context.TestContextAnnotationUtils.findMergedAnnotation
import org.springframework.test.context.TestContextBootstrapper

/**
 * [TestContextBootstrapper] for [@KomapperJdbcTest][KomapperJdbcTest] support.
 */
internal class KomapperJdbcTestContextBootstrapper : SpringBootTestContextBootstrapper() {
    override fun getProperties(testClass: Class<*>): Array<String>? {
        return findMergedAnnotation(testClass, KomapperJdbcTest::class.java)?.properties
    }
}
