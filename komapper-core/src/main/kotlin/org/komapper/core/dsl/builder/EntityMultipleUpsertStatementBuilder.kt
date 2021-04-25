package org.komapper.core.dsl.builder

import org.komapper.core.data.Statement

interface EntityMultipleUpsertStatementBuilder<ENTITY : Any> {
    fun build(): Statement
}
