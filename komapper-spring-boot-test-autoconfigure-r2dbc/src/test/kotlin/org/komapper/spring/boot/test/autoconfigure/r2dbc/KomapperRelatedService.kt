package org.komapper.spring.boot.test.autoconfigure.r2dbc

import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Component

@Component
internal class KomapperRelatedService(
    private val db: R2dbcDatabase,
)
