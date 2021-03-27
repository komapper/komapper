package org.komapper.ksp

import com.google.devtools.ksp.symbol.Nullability
import java.io.PrintWriter

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
        w.println("import java.time.Clock")
        w.println("import org.komapper.core.metamodel.*")
        w.println()
        w.println("@Suppress(\"ClassName\")")
        w.println("class $simpleName : EntityMetamodel<$entityTypeName> {")

        w.println("    private object EntityDescriptor {")
        val idGenerator = entity.idGenerator
        if (idGenerator != null) {
            when (val kind = idGenerator.kind) {
                is IdGeneratorKind.Identity -> {
                    w.println("        val ${idGenerator.name} = IdentityGeneratorDescriptor<$entityTypeName, ${idGenerator.property.typeName}>(${idGenerator.property.typeName}::class)")
                }
                is IdGeneratorKind.Sequence -> {
                    w.println("        val ${idGenerator.name} = SequenceGeneratorDescriptor<$entityTypeName, ${idGenerator.property.typeName}>(${idGenerator.property.typeName}::class, \"${kind.name}\", ${kind.incrementBy})")
                }
            }
        }
        for (p in entity.properties) {
            w.println("        val $p = PropertyDescriptor<$entityTypeName, ${p.typeName}>(${p.typeName}::class, \"${p.columnName}\", { it.$p }) { e, v -> e.copy($p = v) }")
        }
        w.println("    }")

        for (p in entity.properties) {
            w.println("    val $p by lazy { PropertyMetamodelImpl(this, EntityDescriptor.$p) }")
        }
        w.println("    override fun tableName() = \"${entity.tableName}\"")
        w.print("    override fun idAssignment(): Assignment<$entityTypeName>? = ")
        if (idGenerator != null) {
            w.println("EntityDescriptor.${idGenerator.property}__generator.createAssignment(${idGenerator.property}.setter)")
        } else {
            w.println("null")
        }
        val idNameList = entity.idProperties.joinToString { it.toString() }
        w.println("    override fun idProperties(): List<PropertyMetamodel<$entityTypeName, *>> = listOf($idNameList)")
        w.println("    override fun versionProperty(): PropertyMetamodel<$entityTypeName, *>? = ${entity.versionProperty}")
        val allNameList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { it.toString() }
        w.println("    override fun properties(): List<PropertyMetamodel<$entityTypeName, *>> = listOf($allNameList)")
        val argList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { p ->
            val nullability = if (p.nullability == Nullability.NULLABLE) "?" else ""
            "$p = m[$p] as ${p.typeName}$nullability"
        }
        w.println("    override fun instantiate(m: Map<PropertyMetamodel<*, *>, Any?>) = $entityTypeName($argList)")
        val incrementVersionBody = if (entity.versionProperty == null) {
            "e"
        } else {
            "${entity.versionProperty}.setter(e, ${entity.versionProperty}.getter(e)!!.inc())"
        }
        w.println("    override fun incrementVersion(e: $entityTypeName): $entityTypeName = $incrementVersionBody")
        val setCreationClockBody = if (entity.createdAtProperty == null) {
            "e"
        } else {
            "${entity.createdAtProperty}.setter(e, java.time.LocalDateTime.now(c))"
        }
        w.println("    override fun updateCreatedAt(e: $entityTypeName, c: Clock): $entityTypeName = $setCreationClockBody")
        val setUpdateClockBody = if (entity.updatedAtProperty == null) {
            "e"
        } else {
            "${entity.updatedAtProperty}.setter(e, java.time.LocalDateTime.now(c))"
        }
        w.println("    override fun updateUpdatedAt(e: $entityTypeName, c: Clock): $entityTypeName = $setUpdateClockBody")
        w.println("}")
        if (entity.declaration.hasCompanionObject()) {
            w.println("")
            w.println("fun $entityTypeName.Companion.metamodel() = $simpleName()")
        }
    }
}
