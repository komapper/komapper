package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.context.columnAndAliasPairs
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.RecordImpl
import java.time.Clock
import kotlin.reflect.KClass

internal data class InlineViewMetamodel(val context: SubqueryContext) :
    EntityMetamodel<Record, List<Any>, InlineViewMetamodel> {

    override fun declaration(): EntityMetamodelDeclaration<InlineViewMetamodel> {
        return {}
    }

    override fun idGenerator(): IdGenerator<Record, List<Any>>? {
        return null
    }

    override fun idProperties(): List<PropertyMetamodel<Record, *, *>> {
        return properties()
    }

    override fun versionProperty(): PropertyMetamodel<Record, *, *>? {
        return null
    }

    override fun createdAtProperty(): PropertyMetamodel<Record, *, *>? {
        return null
    }

    override fun updatedAtProperty(): PropertyMetamodel<Record, *, *>? {
        return null
    }

    override fun properties(): List<PropertyMetamodel<Record, *, *>> {
        return context.columnAndAliasPairs.map { (column, alias) ->
            InlineViewPropertyMetamodel(this, column, alias)
        }
    }

    override fun convertToId(generatedKey: Long): List<Any>? {
        return null
    }

    override fun versionAssignment(): Pair<PropertyMetamodel<Record, *, *>, Operand>? {
        return null
    }

    override fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<Record, *, *>, Operand>? {
        return null
    }

    override fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<Record, *, *>, Operand>? {
        return null
    }

    override fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): Record {
        @Suppress("UNCHECKED_CAST")
        m as Map<ColumnExpression<*, *>, Any?>
        return RecordImpl(m)
    }

    override fun newMetamodel(
        table: String,
        catalog: String,
        schema: String,
        alwaysQuote: Boolean,
        disableSequenceAssignment: Boolean,
        declaration: EntityMetamodelDeclaration<InlineViewMetamodel>,
    ): InlineViewMetamodel {
        throw UnsupportedOperationException()
    }

    override fun postUpdate(e: Record): Record {
        return e
    }

    override fun preUpdate(e: Record, c: Clock): Record {
        return e
    }

    override fun preInsert(e: Record, c: Clock): Record {
        return e
    }

    override fun extractId(e: Record): List<Any> {
        return properties()
    }

    override fun klass(): KClass<Record> {
        return Record::class
    }

    override fun tableName(): String {
        return ""
    }

    override fun catalogName(): String {
        return ""
    }

    override fun schemaName(): String {
        return ""
    }

    override fun alwaysQuote(): Boolean {
        return false
    }

    override fun disableSequenceAssignment(): Boolean {
        return true
    }
}

internal data class InlineViewPropertyMetamodel<EXTERIOR : Any, INTERIOR : Any>(
    override val owner: EntityMetamodel<Record, *, *>,
    private val delegatee: ColumnExpression<EXTERIOR, INTERIOR>,
    override val columnName: String,
) :
    PropertyMetamodel<Record, EXTERIOR, INTERIOR> {
    override val name: String get() = columnName
    override val getter: (Record) -> EXTERIOR? get() = throw UnsupportedOperationException()
    override val setter: (Record, EXTERIOR) -> Record get() = throw UnsupportedOperationException()
    override val nullable: Boolean get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<EXTERIOR> get() = delegatee.exteriorClass
    override val interiorClass: KClass<INTERIOR> get() = delegatee.interiorClass
    override val wrap: (INTERIOR) -> EXTERIOR get() = delegatee.wrap
    override val unwrap: (EXTERIOR) -> INTERIOR get() = delegatee.unwrap
    override val alwaysQuote: Boolean get() = true
    override val masking: Boolean get() = delegatee.masking
}
