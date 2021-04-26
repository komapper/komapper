package org.komapper.core.dsl.builder

import org.komapper.core.Statement

interface EntityUpsertStatementBuilder<ENTITY : Any> {
    fun build(): Statement
}
