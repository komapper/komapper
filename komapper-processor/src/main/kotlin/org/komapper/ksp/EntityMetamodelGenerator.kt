package org.komapper.ksp

import com.google.devtools.ksp.symbol.Nullability
import java.io.PrintWriter

private const val Assignment = "org.komapper.core.metamodel.Assignment"
private const val EntityMetamodel = "org.komapper.core.metamodel.EntityMetamodel"
private const val IdentityGeneratorDescriptor = "org.komapper.core.metamodel.IdentityGeneratorDescriptor"
private const val SequenceGeneratorDescriptor = "org.komapper.core.metamodel.SequenceGeneratorDescriptor"
private const val PropertyDescriptor = "org.komapper.core.metamodel.PropertyDescriptor"
private const val PropertyMetamodel = "org.komapper.core.metamodel.PropertyMetamodel"
private const val PropertyMetamodelImpl = "org.komapper.core.metamodel.PropertyMetamodelImpl"
private const val Clock = "java.time.Clock"
private const val EntityDescriptor = "__EntityDescriptor"

internal class EntityMetamodelGenerator(
    private val entity: Entity,
    private val packageName: String,
    private val entityTypeName: String,
    private val simpleName: String,
    private val w: PrintWriter
) : Runnable {
    override fun run() {
        w.println("package $packageName")
        w.println()
        w.println("@Suppress(\"ClassName\")")
        w.println("class $simpleName : $EntityMetamodel<$entityTypeName> {")

        entityDescriptor()

        propertyMetamodels()

        tableName()
        idAssignment()
        idProperties()
        versionProperty()
        properties()
        instantiate()
        incrementVersion()
        updateCreatedAt()
        updateUpdatedAt()

        w.println("}")

        companionMetamodel()
    }

    private fun entityDescriptor() {
        w.println("    private object $EntityDescriptor {")
        val idGenerator = entity.idGenerator
        if (idGenerator != null) {
            when (val kind = idGenerator.kind) {
                is IdGeneratorKind.Identity -> {
                    w.println("        val ${idGenerator.name} = $IdentityGeneratorDescriptor<$entityTypeName, ${idGenerator.property.typeName}>(${idGenerator.property.typeName}::class)")
                }
                is IdGeneratorKind.Sequence -> {
                    w.println("        val ${idGenerator.name} = $SequenceGeneratorDescriptor<$entityTypeName, ${idGenerator.property.typeName}>(${idGenerator.property.typeName}::class, \"${kind.name}\", ${kind.incrementBy})")
                }
            }
        }
        for (p in entity.properties) {
            w.println("        val $p = $PropertyDescriptor<$entityTypeName, ${p.typeName}>(${p.typeName}::class, \"${p.columnName}\", { it.$p }) { e, v -> e.copy($p = v) }")
        }
        w.println("    }")
    }

    private fun propertyMetamodels() {
        for (p in entity.properties) {
            w.println("    val $p by lazy { $PropertyMetamodelImpl(this, $EntityDescriptor.$p) }")
        }
    }

    private fun tableName() {
        w.println("    override fun tableName() = \"${entity.tableName}\"")
    }

    private fun idAssignment() {
        val idGenerator = entity.idGenerator
        w.print("    override fun idAssignment(): $Assignment<$entityTypeName>? = ")
        if (idGenerator != null) {
            w.println("$EntityDescriptor.${idGenerator.name}.createAssignment(${idGenerator.property}.setter)")
        } else {
            w.println("null")
        }
    }

    private fun idProperties() {
        val idNameList = entity.idProperties.joinToString { it.toString() }
        w.println("    override fun idProperties(): List<$PropertyMetamodel<$entityTypeName, *>> = listOf($idNameList)")
    }

    private fun versionProperty() {
        w.println("    override fun versionProperty(): $PropertyMetamodel<$entityTypeName, *>? = ${entity.versionProperty}")
    }

    private fun properties() {
        val allNameList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { it.toString() }
        w.println("    override fun properties(): List<$PropertyMetamodel<$entityTypeName, *>> = listOf($allNameList)")
    }

    private fun instantiate() {
        val argList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { p ->
            val nullability = if (p.nullability == Nullability.NULLABLE) "?" else ""
            "$p = __m[$p] as ${p.typeName}$nullability"
        }
        w.println("    override fun instantiate(__m: Map<$PropertyMetamodel<*, *>, Any?>) = $entityTypeName($argList)")
    }

    private fun incrementVersion() {
        val body = if (entity.versionProperty == null) {
            "__e"
        } else {
            "${entity.versionProperty}.setter(__e, ${entity.versionProperty}.getter(__e)!!.inc())"
        }
        w.println("    override fun incrementVersion(__e: $entityTypeName): $entityTypeName = $body")
    }

    private fun updateCreatedAt() {
        val property = entity.createdAtProperty
        val body = if (property == null) {
            "__e"
        } else {
            "$property.setter(__e, ${property.typeName}.now(__c))"
        }
        w.println("    override fun updateCreatedAt(__e: $entityTypeName, __c: $Clock): $entityTypeName = $body")
    }

    private fun updateUpdatedAt() {
        val property = entity.updatedAtProperty
        val body = if (property == null) {
            "__e"
        } else {
            "$property.setter(__e, ${property.typeName}.now(__c))"
        }
        w.println("    override fun updateUpdatedAt(__e: $entityTypeName, __c: $Clock): $entityTypeName = $body")
    }

    private fun companionMetamodel() {
        if (entity.declaration.hasCompanionObject()) {
            w.println("")
            w.println("fun $entityTypeName.Companion.metamodel() = $simpleName()")
        }
    }
}
