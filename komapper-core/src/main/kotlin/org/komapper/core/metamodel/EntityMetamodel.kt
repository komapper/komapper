package org.komapper.core.metamodel

import java.time.Clock

interface EntityMetamodel<ENTITY> : TableInfo {
    fun idAssignment(): Assignment<ENTITY>?
    fun idProperties(): List<PropertyMetamodel<ENTITY, *>>
    fun versionProperty(): PropertyMetamodel<ENTITY, *>?
    fun properties(): List<PropertyMetamodel<ENTITY, *>>
    fun instantiate(m: Map<PropertyMetamodel<*, *>, Any?>): ENTITY
    fun incrementVersion(e: ENTITY): ENTITY
    fun updateCreatedAt(e: ENTITY, c: Clock): ENTITY
    fun updateUpdatedAt(e: ENTITY, c: Clock): ENTITY
}

abstract class EmptyEntityMetamodel<ENTITY> : EntityMetamodel<ENTITY> {
    override fun tableName(): String = error("error")
    override fun idAssignment(): Assignment<ENTITY>? = error("error")
    override fun idProperties(): List<PropertyMetamodel<ENTITY, *>> = error("error")
    override fun versionProperty(): PropertyMetamodel<ENTITY, *>? = error("error")
    override fun properties(): List<PropertyMetamodel<ENTITY, *>> = error("error")
    override fun instantiate(m: Map<PropertyMetamodel<*, *>, Any?>): ENTITY = error("error")
    override fun incrementVersion(e: ENTITY): ENTITY = error("error")
    override fun updateCreatedAt(e: ENTITY, c: Clock): ENTITY = error("error")
    override fun updateUpdatedAt(e: ENTITY, c: Clock): ENTITY = error("error")
}
