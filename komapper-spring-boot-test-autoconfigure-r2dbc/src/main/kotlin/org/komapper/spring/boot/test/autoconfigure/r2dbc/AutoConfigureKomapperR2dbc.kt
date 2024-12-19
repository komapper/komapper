package org.komapper.spring.boot.test.autoconfigure.r2dbc

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import java.lang.annotation.Inherited

/**
 * [Auto-configuration imports][ImportAutoConfiguration] for typical Komapper JDBC tests.
 * Most tests should consider using [@KomapperR2dbcTest][KomapperR2dbcTest] rather than using this annotation directly.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@ImportAutoConfiguration
annotation class AutoConfigureKomapperR2dbc
