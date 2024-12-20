package org.komapper.spring.boot.test.autoconfigure.jdbc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.komapper.jdbc.JdbcDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.context.ConfigurableApplicationContext

@KomapperJdbcTest(properties = ["spring.datasource.url=jdbc:h2:mem:example"])
open class KomapperJdbcTestTest(
    @Autowired
    private val applicationContext: ConfigurableApplicationContext,
) {
    @Test
    fun `only Komapper-related beans are loaded`() {
        assertThat(AssertableApplicationContext.get { applicationContext })
            .hasNotFailed()
            .hasSingleBean(JdbcDatabase::class.java)
            .hasSingleBean(KomapperRelatedService::class.java)
            .doesNotHaveBean(UnrelatedService::class.java)
    }
}
