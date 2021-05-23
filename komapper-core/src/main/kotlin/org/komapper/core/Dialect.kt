package org.komapper.core

import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.OffsetLimitStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import kotlin.reflect.KClass

interface Dialect {
    val openQuote: String
    val closeQuote: String
    val escapeSequence: String

    fun formatValue(value: Any?, valueClass: KClass<*>): String
    fun enquote(name: String): String
    fun escape(text: String, escapeSequence: String? = null): String
    fun getDataTypeName(klass: KClass<*>): String
    fun getOffsetLimitStatementBuilder(offset: Int, limit: Int): OffsetLimitStatementBuilder
    fun getSequenceSql(sequenceName: String): String
    fun getSchemaStatementBuilder(): SchemaStatementBuilder

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>
    ): EntityUpsertStatementBuilder<ENTITY>
}
