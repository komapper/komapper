package org.komapper.template

import org.komapper.core.BuilderDialect
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.core.template.expression.CacheExprNodeFactory
import org.komapper.core.template.expression.NoCacheExprNodeFactory
import org.komapper.core.template.sql.CacheSqlNodeFactory
import org.komapper.core.template.sql.NoCacheSqlNodeFactory

class TwoWayTemplateStatementBuilderFactory : TemplateStatementBuilderFactory {

    override fun create(dialect: BuilderDialect, enableCache: Boolean): TemplateStatementBuilder {
        val sqlNodeFactory = if (enableCache) CacheSqlNodeFactory() else NoCacheSqlNodeFactory()
        val exprNodeFactory = if (enableCache) CacheExprNodeFactory() else NoCacheExprNodeFactory()
        val exprEnvironment = DefaultExprEnvironment()
        val exprEvaluator = DefaultExprEvaluator(exprNodeFactory, exprEnvironment)
        return TwoWayTemplateStatementBuilder(dialect, sqlNodeFactory, exprEvaluator)
    }
}
