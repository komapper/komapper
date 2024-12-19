package org.komapper.spring.boot.test.autoconfigure.r2dbc

import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.test.context.TestContextAnnotationUtils.findMergedAnnotation
import org.springframework.test.context.TestContextBootstrapper

/**
 * [TestContextBootstrapper] for [@KomapperR2dbcTest][KomapperR2dbcTest] support.
 */
internal class KomapperR2dbcTestContextBootstrapper : SpringBootTestContextBootstrapper() {
    override fun getProperties(testClass: Class<*>): Array<String>? {
        return findMergedAnnotation(testClass, KomapperR2dbcTest::class.java)?.properties
    }
}
