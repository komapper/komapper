package org.komapper.core.query

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
data class Emp(
    @KmId val id: Int = 0,
    val addressId: Int,
    @KmVersion val version: Int = 0
) {
    companion object
}

@Suppress("ClassName")
class Emp_ : EntityMetamodel<Emp> {
    private object EntityDescriptor {
        val id = PropertyDescriptor<Emp, Int>(Int::class, "ID", { it.id }) { (e, v) -> e.copy(id = v) }
        val addressId =
            PropertyDescriptor<Emp, Int>(Int::class, "ADDRESS_ID", { it.addressId }) { (e, v) -> e.copy(addressId = v) }
        val version =
            PropertyDescriptor<Emp, Int>(Int::class, "VERSION", { it.version }) { (e, v) -> e.copy(version = v) }
    }

    val id by lazy { PropertyMetamodelImpl(this, EntityDescriptor.id) }
    val addressId by lazy { PropertyMetamodelImpl(this, EntityDescriptor.addressId) }
    val version by lazy { PropertyMetamodelImpl(this, EntityDescriptor.version) }
    override fun tableName() = "EMP"
    override fun idAssignment(): Assignment<Emp>? = null
    override fun idProperties(): List<PropertyMetamodel<Emp, *>> = listOf(id)
    override fun versionProperty(): PropertyMetamodel<Emp, *>? = version
    override fun properties(): List<PropertyMetamodel<Emp, *>> = listOf(id, addressId, version)
    override fun instantiate(m: Map<PropertyMetamodel<*, *>, Any?>) =
        Emp(m[id] as Int, m[addressId] as Int, m[version] as Int)

    override fun incrementVersion(e: Emp): Emp = version.set(e to version.get(e)!!.inc())
    override fun updateCreatedAt(e: Emp, c: Clock): Emp = e
    override fun updateUpdatedAt(e: Emp, c: Clock): Emp = e
}

fun Emp.Companion.metamodel() = Emp_()
