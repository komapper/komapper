package org.komapper.template

import org.komapper.core.Dialect
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.template.expression.CacheExprNodeFactory
import org.komapper.template.expression.DefaultExprEnvironment
import org.komapper.template.expression.DefaultExprEvaluator
import org.komapper.template.expression.NoCacheExprNodeFactory
import org.komapper.template.sql.CacheSqlNodeFactory
import org.komapper.template.sql.NoCacheSqlNodeFactory

class DefaultTemplateStatementBuilderFactory : TemplateStatementBuilderFactory {

    override fun create(dialect: Dialect, cache: Boolean): TemplateStatementBuilder {
        val exprNodeFactory = if (cache) CacheExprNodeFactory() else NoCacheExprNodeFactory()
        val exprEnvironment = DefaultExprEnvironment()
        val exprEvaluator = DefaultExprEvaluator(exprNodeFactory, exprEnvironment)
        val sqlNodeFactory = if (cache) CacheSqlNodeFactory() else NoCacheSqlNodeFactory()
        return DefaultTemplateStatementBuilder(dialect::formatValue, sqlNodeFactory, exprEvaluator)
    }
}
