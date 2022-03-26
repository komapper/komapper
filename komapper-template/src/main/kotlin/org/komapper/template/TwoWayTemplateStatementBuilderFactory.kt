package org.komapper.template

import org.komapper.core.BuilderDialect
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.template.expression.CacheExprNodeFactory
import org.komapper.template.expression.DefaultExprEnvironment
import org.komapper.template.expression.DefaultExprEvaluator
import org.komapper.template.expression.NoCacheExprNodeFactory
import org.komapper.template.sql.CacheSqlNodeFactory
import org.komapper.template.sql.NoCacheSqlNodeFactory

class TwoWayTemplateStatementBuilderFactory : TemplateStatementBuilderFactory {

    override fun create(dialect: BuilderDialect, enableCache: Boolean): TemplateStatementBuilder {
        val sqlNodeFactory = if (enableCache) CacheSqlNodeFactory() else NoCacheSqlNodeFactory()
        val exprNodeFactory = if (enableCache) CacheExprNodeFactory() else NoCacheExprNodeFactory()
        val exprEnvironment = DefaultExprEnvironment()
        val exprEvaluator = DefaultExprEvaluator(exprNodeFactory, exprEnvironment)
        return TwoWayTemplateStatementBuilder(dialect, sqlNodeFactory, exprEvaluator)
    }
}
