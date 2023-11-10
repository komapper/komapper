package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.TableExpression
import java.time.Clock
import kotlin.reflect.KClass

@ThreadSafe
interface EntityMetamodel<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : TableExpression<ENTITY> {
    companion object {
        /**
         * The version of the metamodel.
         * This version will be incremented as the specification changes.
         */
        const val METAMODEL_VERSION: Int = 1
    }
    fun declaration(): EntityMetamodelDeclaration<META>
    fun idGenerator(): IdGenerator<ENTITY, ID>?
    fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>>
    fun virtualIdProperties(): List<PropertyMetamodel<ENTITY, *, *>> = emptyList()
    fun versionProperty(): PropertyMetamodel<ENTITY, *, *>?
    fun createdAtProperty(): PropertyMetamodel<ENTITY, *, *>?
    fun updatedAtProperty(): PropertyMetamodel<ENTITY, *, *>?
    fun properties(): List<PropertyMetamodel<ENTITY, *, *>>
    fun extractId(e: ENTITY): ID
    fun convertToId(generatedKey: Long): ID?
    fun versionAssignment(): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?
    fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?
    fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?
    fun preInsert(e: ENTITY, c: Clock): ENTITY
    fun preUpdate(e: ENTITY, c: Clock): ENTITY
    fun postUpdate(e: ENTITY): ENTITY
    fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): ENTITY
    fun newMetamodel(
        table: String,
        catalog: String,
        schema: String,
        alwaysQuote: Boolean,
        disableSequenceAssignment: Boolean,
        declaration: EntityMetamodelDeclaration<META>,
    ): META
}

@Suppress("unused")
abstract class EntityMetamodelStub<ENTITY : Any, META : EntityMetamodelStub<ENTITY, META>> :
    EntityMetamodel<ENTITY, Any, META> {
    override fun klass(): KClass<ENTITY> = fail()
    override fun tableName(): String = fail()
    override fun catalogName(): String = fail()
    override fun schemaName(): String = fail()
    override fun alwaysQuote(): Boolean = fail()
    override fun disableSequenceAssignment(): Boolean = fail()
    override fun declaration(): EntityMetamodelDeclaration<META> = fail()
    override fun idGenerator(): IdGenerator<ENTITY, Any>? = fail()
    override fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>> = fail()
    override fun versionProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun createdAtProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun updatedAtProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun properties(): List<PropertyMetamodel<ENTITY, *, *>> = fail()
    override fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): ENTITY = fail()
    override fun extractId(e: ENTITY): Any = fail()
    override fun convertToId(generatedKey: Long): Any = fail()
    override fun versionAssignment(): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = fail()
    override fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = fail()
    override fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = fail()
    override fun preInsert(e: ENTITY, c: Clock): ENTITY = fail()
    override fun preUpdate(e: ENTITY, c: Clock): ENTITY = fail()
    override fun postUpdate(e: ENTITY): ENTITY = fail()
    override fun newMetamodel(
        table: String,
        catalog: String,
        schema: String,
        alwaysQuote: Boolean,
        disableSequenceAssignment: Boolean,
        declaration: EntityMetamodelDeclaration<META>,
    ): META = fail()

    private fun fail(): Nothing {
        error("Fix google/ksp compile errors.")
    }
}

internal object EmptyMetamodel : EntityMetamodel<Nothing, Nothing, EmptyMetamodel> {
    override fun declaration(): EntityMetamodelDeclaration<EmptyMetamodel> {
        return {}
    }

    override fun idGenerator(): IdGenerator<Nothing, Nothing>? {
        return null
    }

    override fun idProperties(): List<PropertyMetamodel<Nothing, *, *>> {
        return emptyList()
    }

    override fun versionProperty(): PropertyMetamodel<Nothing, *, *>? {
        return null
    }

    override fun createdAtProperty(): PropertyMetamodel<Nothing, *, *>? {
        return null
    }

    override fun updatedAtProperty(): PropertyMetamodel<Nothing, *, *>? {
        return null
    }

    override fun properties(): List<PropertyMetamodel<Nothing, *, *>> {
        return emptyList()
    }

    override fun convertToId(generatedKey: Long): Nothing? {
        return null
    }

    override fun versionAssignment(): Pair<PropertyMetamodel<Nothing, *, *>, Operand>? {
        return null
    }

    override fun createdAtAssignment(c: Clock): Pair<PropertyMetamodel<Nothing, *, *>, Operand>? {
        return null
    }

    override fun updatedAtAssignment(c: Clock): Pair<PropertyMetamodel<Nothing, *, *>, Operand>? {
        return null
    }

    override fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): Nothing {
        throw UnsupportedOperationException()
    }

    override fun newMetamodel(
        table: String,
        catalog: String,
        schema: String,
        alwaysQuote: Boolean,
        disableSequenceAssignment: Boolean,
        declaration: EntityMetamodelDeclaration<EmptyMetamodel>,
    ): EmptyMetamodel {
        throw UnsupportedOperationException()
    }

    override fun postUpdate(e: Nothing): Nothing {
        return e
    }

    override fun preUpdate(e: Nothing, c: Clock): Nothing {
        return e
    }

    override fun preInsert(e: Nothing, c: Clock): Nothing {
        return e
    }

    override fun extractId(e: Nothing): Nothing {
        throw UnsupportedOperationException()
    }

    override fun klass(): KClass<Nothing> {
        throw UnsupportedOperationException()
    }

    override fun tableName(): String {
        throw UnsupportedOperationException()
    }

    override fun catalogName(): String {
        throw UnsupportedOperationException()
    }

    override fun schemaName(): String {
        throw UnsupportedOperationException()
    }

    override fun alwaysQuote(): Boolean {
        return false
    }

    override fun disableSequenceAssignment(): Boolean {
        return false
    }
}
