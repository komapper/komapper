package org.komapper.sqlcommenter

import org.komapper.core.StatementInspector
import org.komapper.core.spi.StatementInspectorFactory

class SqlCommenterFactory : StatementInspectorFactory {
    override fun create(): StatementInspector {
        return SqlCommenter()
    }
}
