package org.komapper.spring.boot.test.autoconfigure.jdbc

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import java.lang.annotation.Inherited

/**
 * [Auto-configuration imports][ImportAutoConfiguration] for typical Komapper JDBC tests.
 * Most tests should consider using [@KomapperJdbcTest][KomapperJdbcTest] rather than using this annotation directly.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@ImportAutoConfiguration
annotation class AutoConfigureKomapperJdbc
