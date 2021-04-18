package org.komapper.core.dsl.builder

import org.komapper.core.data.Statement

interface EntityMultiUpsertStatementBuilder<ENTITY : Any> {
    fun build(): Statement
}
