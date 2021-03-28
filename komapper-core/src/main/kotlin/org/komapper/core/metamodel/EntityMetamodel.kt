package org.komapper.core.metamodel

import java.time.Clock

interface EntityMetamodel<ENTITY> : TableInfo {
    fun idAssignment(): Assignment<ENTITY>?
    fun idProperties(): List<PropertyMetamodel<ENTITY, *>>
    fun versionProperty(): PropertyMetamodel<ENTITY, *>?
    fun properties(): List<PropertyMetamodel<ENTITY, *>>
    fun instantiate(__m: Map<PropertyMetamodel<*, *>, Any?>): ENTITY
    fun incrementVersion(__e: ENTITY): ENTITY
    fun updateCreatedAt(__e: ENTITY, __c: Clock): ENTITY
    fun updateUpdatedAt(__e: ENTITY, __c: Clock): ENTITY
}

@Suppress("unused")
abstract class EmptyEntityMetamodel<ENTITY> : EntityMetamodel<ENTITY> {
    override fun tableName(): String = fail()
    override fun catalogName(): String = fail()
    override fun schemaName(): String = fail()
    override fun idAssignment(): Assignment<ENTITY>? = fail()
    override fun idProperties(): List<PropertyMetamodel<ENTITY, *>> = fail()
    override fun versionProperty(): PropertyMetamodel<ENTITY, *>? = fail()
    override fun properties(): List<PropertyMetamodel<ENTITY, *>> = fail()
    override fun instantiate(__m: Map<PropertyMetamodel<*, *>, Any?>): ENTITY = fail()
    override fun incrementVersion(__e: ENTITY): ENTITY = fail()
    override fun updateCreatedAt(__e: ENTITY, __c: Clock): ENTITY = fail()
    override fun updateUpdatedAt(__e: ENTITY, __c: Clock): ENTITY = fail()

    private fun fail(): Nothing {
        error("Fix a compile error.")
    }
}
