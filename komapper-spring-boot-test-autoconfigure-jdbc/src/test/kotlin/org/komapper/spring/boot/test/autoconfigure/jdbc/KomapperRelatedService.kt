package org.komapper.spring.boot.test.autoconfigure.jdbc

import org.komapper.jdbc.JdbcDatabase
import org.springframework.stereotype.Component

@Component
internal class KomapperRelatedService(
    private val db: JdbcDatabase,
)
