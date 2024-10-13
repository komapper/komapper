package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.TemplateSelectOptions

/**
 * The builder of [TemplateSelectQuery].
 */
@ThreadSafe
interface TemplateSelectQueryBuilder : TemplateBinder<TemplateSelectQueryBuilder> {
    /**
     * Builds a builder with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the builder
     */
    fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder

    /**
     * Builds a query that returns a list.
     *
     * @param T the element type of list
     * @param transform the function to transform a [Row] type to an arbitrary type
     * @return the query
     */
    fun <T> select(transform: (Row) -> T): TemplateSelectQuery<T>

    /**
     * Builds a query that selects multiple columns and transforms a row into an entity.
     *
     * The type, number, and order of columns in the row must match the constructor of the entity.
     *
     * @param ENTITY the entity type
     * @param metamodel the entity metamodel
     * @param strategy the projection strategy
     * @return the query that returns a list of entity
     */
    fun <ENTITY : Any> selectAsEntity(
        metamodel: EntityMetamodel<ENTITY, *, *>,
        strategy: ProjectionType = ProjectionType.INDEX,
    ): TemplateSelectQuery<ENTITY>
}

internal data class TemplateSelectQueryBuilderImpl(
    private val context: TemplateSelectContext,
) : TemplateSelectQueryBuilder {
    override fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun bindValue(name: String, value: Value<*>): TemplateSelectQueryBuilder {
        val newContext = context.copy(valueMap = context.valueMap + (name to value))
        return copy(context = newContext)
    }

    override fun <T> select(transform: (Row) -> T): TemplateSelectQuery<T> {
        return TemplateSelectQueryImpl(context, transform)
    }

    override fun <ENTITY : Any> selectAsEntity(metamodel: EntityMetamodel<ENTITY, *, *>, strategy: ProjectionType): TemplateSelectQuery<ENTITY> {
        return TemplateEntityProjectionSelectQuery(context, metamodel, strategy)
    }
}
