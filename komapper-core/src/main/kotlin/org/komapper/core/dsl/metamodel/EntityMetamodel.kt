package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.TableExpression
import java.time.Clock
import kotlin.reflect.KClass

@ThreadSafe
interface EntityMetamodel<ENTITY : Any, ID, out META : EntityMetamodel<ENTITY, ID, META>> : TableExpression<ENTITY> {
    fun idAssignment(): IdAssignment<ENTITY>?
    fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>>
    fun versionProperty(): PropertyMetamodel<ENTITY, *, *>?
    fun createdAtProperty(): PropertyMetamodel<ENTITY, *, *>?
    fun updatedAtProperty(): PropertyMetamodel<ENTITY, *, *>?
    fun properties(): List<PropertyMetamodel<ENTITY, *, *>>
    fun getId(e: ENTITY): ID
    fun toId(generatedKey: Long): ID?
    fun preInsert(e: ENTITY, c: Clock): ENTITY
    fun preUpdate(e: ENTITY, c: Clock): ENTITY
    fun postUpdate(e: ENTITY): ENTITY
    fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): ENTITY
    fun newMeta(table: String, catalog: String, schema: String, alwaysQuote: Boolean, disableSequenceAssignment: Boolean): META
}

@Suppress("unused")
abstract class EntityMetamodelStub<ENTITY : Any, META : EntityMetamodelStub<ENTITY, META>> :
    EntityMetamodel<ENTITY, Any, META> {
    override fun klass(): KClass<ENTITY> = fail()
    override fun tableName(): String = fail()
    override fun catalogName(): String = fail()
    override fun schemaName(): String = fail()
    override fun alwaysQuote(): Boolean = fail()
    override fun idAssignment(): IdAssignment<ENTITY>? = fail()
    override fun idProperties(): List<PropertyMetamodel<ENTITY, *, *>> = fail()
    override fun versionProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun createdAtProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun updatedAtProperty(): PropertyMetamodel<ENTITY, *, *>? = fail()
    override fun properties(): List<PropertyMetamodel<ENTITY, *, *>> = fail()
    override fun newEntity(m: Map<PropertyMetamodel<*, *, *>, Any?>): ENTITY = fail()
    override fun getId(e: ENTITY): Any = fail()
    override fun toId(generatedKey: Long): Any = fail()
    override fun preInsert(e: ENTITY, c: Clock): ENTITY = fail()
    override fun preUpdate(e: ENTITY, c: Clock): ENTITY = fail()
    override fun postUpdate(e: ENTITY): ENTITY = fail()
    override fun newMeta(table: String, catalog: String, schema: String, alwaysQuote: Boolean, disableSequenceAssignment: Boolean): META = fail()

    private fun fail(): Nothing {
        error("Fix google/ksp compile errors.")
    }
}
