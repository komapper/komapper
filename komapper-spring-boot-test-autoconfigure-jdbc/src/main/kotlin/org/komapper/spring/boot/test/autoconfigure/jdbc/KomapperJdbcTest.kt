package org.komapper.spring.boot.test.autoconfigure.jdbc

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.annotation.AliasFor
import org.springframework.core.env.Environment
import org.springframework.test.context.BootstrapWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Annotation for a Komapper JDBC test that focuses **only** on Komapper JDBC-based components.
 *
 * Using this annotation only enables auto-configuration that is relevant to Komapper JDBC tests.
 * Similarly, component scanning is configured to skip regular components and
 * configuration properties.
 *
 * By default, tests annotated with `@KomapperJdbcTest` use the configured database. If you
 * want to replace any explicit or usually auto-configured DataSource by an embedded
 * in-memory database, the [@AutoConfigureTestDatabase][AutoConfigureTestDatabase]
 * annotation can be used to override these settings.
 *
 * When using JUnit 4, this annotation should be used in combination with `@RunWith(SpringRunner.class)`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@BootstrapWith(KomapperJdbcTestContextBootstrapper::class)
@ExtendWith(SpringExtension::class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(KomapperJdbcTypeExcludeFilter::class)
@Transactional
@AutoConfigureCache
@AutoConfigureKomapperJdbc
@ImportAutoConfiguration
annotation class KomapperJdbcTest(

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
