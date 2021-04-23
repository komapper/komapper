package org.komapper.core

import org.komapper.core.data.Statement

interface TemplateStatementBuilder {
    fun build(
        template: CharSequence,
        params: Any,
        escape: (String) -> String
    ): Statement
}
