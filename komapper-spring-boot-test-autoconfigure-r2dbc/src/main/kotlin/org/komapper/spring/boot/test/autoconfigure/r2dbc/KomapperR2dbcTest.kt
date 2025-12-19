package org.komapper.spring.boot.test.autoconfigure.r2dbc

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.filter.annotation.TypeExcludeFilters
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.annotation.AliasFor
import org.springframework.core.env.Environment
import org.springframework.test.context.BootstrapWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Annotation for a Komapper R2DBC test that focuses **only** on Komapper R2DBC-based components.
 *
 * Using this annotation only enables auto-configuration that is relevant to Komapper R2DBC tests.
 * Similarly, component scanning is configured to skip regular components and
 * configuration properties.
 *
 * When using JUnit 4, this annotation should be used in combination with `@RunWith(SpringRunner.class)`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@BootstrapWith(KomapperR2dbcTestContextBootstrapper::class)
@ExtendWith(SpringExtension::class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(KomapperR2dbcTypeExcludeFilter::class)
@AutoConfigureKomapperR2dbc
@ImportAutoConfiguration
annotation class KomapperR2dbcTest(

    /**
     * Properties in form `key=value` that should be added to the Spring [Environment] before the test runs.
     */
    val properties: Array<String> = [],

    /**
     * Determines if default filtering should be used with [@SpringBootApplication][SpringBootApplication].
     * By default, no beans are included.
     *
     * @see includeFilters
     * @see excludeFilters
     */
    val useDefaultFilters: Boolean = true,

    /**
     * A set of include filters which can be used to add otherwise filtered beans
     * to the application context.
     */
    val includeFilters: Array<ComponentScan.Filter> = [],

    /**
     * A set of exclude filters which can be used to filter beans that would otherwise be added
     * to the application context.
     */
    val excludeFilters: Array<ComponentScan.Filter> = [],

    /**
     * Auto-configuration exclusions that should be applied for this test.
     */
    @get:AliasFor(annotation = ImportAutoConfiguration::class, attribute = "exclude")
    val excludeAutoConfiguration: Array<KClass<*>> = [],
)
