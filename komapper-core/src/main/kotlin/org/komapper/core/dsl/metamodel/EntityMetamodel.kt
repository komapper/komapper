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
         * This version will be changed when the specification is changed.
         */
        const val METAMODEL_VERSION: Int = 1
    }
    fun declaration(): EntityMetamodelDeclaration<META>
    fun idGenerator(): IdGenerator<ENTITY, ID>?
    fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>>
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
        declaration: EntityMetamodelDeclaration<META>
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
        declaration: EntityMetamodelDeclaration<META>
    ): META = fail()

    private fun fail(): Nothing {
        error("Fix google/ksp compile errors.")
    }
}
