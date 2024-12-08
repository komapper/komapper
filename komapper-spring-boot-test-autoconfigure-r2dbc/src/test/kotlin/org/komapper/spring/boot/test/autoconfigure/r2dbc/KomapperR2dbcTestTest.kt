package org.komapper.spring.boot.test.autoconfigure.r2dbc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.context.ConfigurableApplicationContext

@KomapperR2dbcTest(properties = ["spring.r2dbc.url=r2dbc:h2:mem:///test"])
open class KomapperR2dbcTestTest(
    @Autowired
    private val applicationContext: ConfigurableApplicationContext,
) {
    @Test
    fun `only Komapper-related beans are loaded`() {
        assertThat(AssertableApplicationContext.get { applicationContext })
            .hasNotFailed()
            .hasSingleBean(R2dbcDatabase::class.java)
            .hasSingleBean(KomapperRelatedService::class.java)
            .doesNotHaveBean(UnrelatedService::class.java)
    }
}
