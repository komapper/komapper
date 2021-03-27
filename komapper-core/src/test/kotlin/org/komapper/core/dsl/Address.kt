package org.komapper.core.dsl

import org.komapper.core.KmEntity
import org.komapper.core.KmId
import org.komapper.core.KmVersion
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyDescriptor
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.metamodel.PropertyMetamodelImpl
import java.time.Clock

@KmEntity
data class Address(
    @KmId val id: Int = 0,
    val street: String,
    @KmVersion val version: Int = 0
) {
    companion object
}

@Suppress("ClassName")
class Address_ : EntityMetamodel<Address> {
    private object EntityDescriptor {
        val id = PropertyDescriptor<Address, Int>(Int::class, "ID", { it.id }) { (e, v) -> e.copy(id = v) }
        val street =
            PropertyDescriptor<Address, String>(String::class, "STREET", { it.street }) { (e, v) -> e.copy(street = v) }
        val version =
            PropertyDescriptor<Address, Int>(Int::class, "VERSION", { it.version }) { (e, v) -> e.copy(version = v) }
    }

    val id by lazy { PropertyMetamodelImpl(this, EntityDescriptor.id) }
    val street by lazy { PropertyMetamodelImpl(this, EntityDescriptor.street) }
    val version by lazy { PropertyMetamodelImpl(this, EntityDescriptor.version) }
    override fun tableName() = "ADDRESS"
    override fun idAssignment(): Assignment<Address>? = null
    override fun idProperties(): List<PropertyMetamodel<Address, *>> = listOf(id)
    override fun versionProperty(): PropertyMetamodel<Address, *> = version
    override fun properties(): List<PropertyMetamodel<Address, *>> = listOf(id, street, version)
    override fun instantiate(m: Map<PropertyMetamodel<*, *>, Any?>) =
        Address(m[id] as Int, m[street] as String, m[version] as Int)

    override fun incrementVersion(e: Address): Address = version.set(e to version.get(e)!!.inc())
    override fun updateCreatedAt(e: Address, c: Clock): Address = e
    override fun updateUpdatedAt(e: Address, c: Clock): Address = e
}

fun Address.Companion.metamodel() = Address_()
