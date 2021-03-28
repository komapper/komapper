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
data class Emp(
    @KmId val id: Int = 0,
    val addressId: Int,
    @KmVersion val version: Int = 0
) {
    companion object
}

@Suppress("ClassName")
class Emp_(val __tableName: String = "EMP", val __catalogName: String = "", val __schemaName: String = "") :
    EntityMetamodel<Emp> {
    private object EntityDescriptor {
        val id = PropertyDescriptor<Emp, Int>(Int::class, "ID", { it.id }) { e, v -> e.copy(id = v) }
        val addressId =
            PropertyDescriptor<Emp, Int>(Int::class, "ADDRESS_ID", { it.addressId }) { e, v -> e.copy(addressId = v) }
        val version =
            PropertyDescriptor<Emp, Int>(Int::class, "VERSION", { it.version }) { e, v -> e.copy(version = v) }
    }

    val id by lazy { PropertyMetamodelImpl(this, EntityDescriptor.id) }
    val addressId by lazy { PropertyMetamodelImpl(this, EntityDescriptor.addressId) }
    val version by lazy { PropertyMetamodelImpl(this, EntityDescriptor.version) }
    override fun tableName() = "EMP"
    override fun catalogName() = ""
    override fun schemaName() = ""
    override fun idAssignment(): Assignment<Emp>? = null
    override fun idProperties(): List<PropertyMetamodel<Emp, *>> = listOf(id)
    override fun versionProperty(): PropertyMetamodel<Emp, *>? = version
    override fun properties(): List<PropertyMetamodel<Emp, *>> = listOf(id, addressId, version)
    override fun instantiate(__m: Map<PropertyMetamodel<*, *>, Any?>) =
        Emp(__m[id] as Int, __m[addressId] as Int, __m[version] as Int)

    override fun incrementVersion(__e: Emp): Emp = version.setter(__e, version.getter(__e)!!.inc())
    override fun updateCreatedAt(__e: Emp, __c: Clock): Emp = __e
    override fun updateUpdatedAt(__e: Emp, __c: Clock): Emp = __e

    companion object {
        fun overrideTable(__tableName: String) = Address_(__tableName = __tableName)
        fun overrideCatalog(__catalogName: String) = Address_(__catalogName = __catalogName)
        fun overrideSchema(__schemaName: String) = Address_(__schemaName = __schemaName)
    }
}

fun Emp.Companion.metamodel() = Emp_()
