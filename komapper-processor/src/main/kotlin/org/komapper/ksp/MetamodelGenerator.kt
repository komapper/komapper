package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Nullability
import java.io.PrintWriter

private const val Assignment = "org.komapper.core.metamodel.Assignment"
private const val EntityMetamodel = "org.komapper.core.metamodel.EntityMetamodel"
private const val EmptyEntityMetamodel = "org.komapper.core.metamodel.EmptyEntityMetamodel"
private const val EmptyPropertyMetamodel = "org.komapper.core.metamodel.EmptyPropertyMetamodel"
private const val Identity = "org.komapper.core.metamodel.IdGeneratorDescriptor.Identity"
private const val Sequence = "org.komapper.core.metamodel.IdGeneratorDescriptor.Sequence"
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

        val paramList = listOf(
            "private val __tableName: String = \"${entity.table.name}\"",
            "private val __catalogName: String = \"${entity.table.catalog}\"",
            "private val __schemaName: String = \"${entity.table.schema}\""
        ).joinToString(", ")
        w.println("class $simpleName($paramList) : $EntityMetamodel<$entityTypeName> {")

        entityDescriptor()

        propertyMetamodels()

        tableName()
        catalogName()
        schemaName()

        idAssignment()
        idProperties()
        versionProperty()
        properties()
        instantiate()
        incrementVersion()
        updateCreatedAt()
        updateUpdatedAt()
        companionObject()

        w.println("}")

        utils()
    }

    private fun entityDescriptor() {
        w.println("    private object $EntityDescriptor {")
        for (p in entity.properties) {
            val nullable = if (p.nullability == Nullability.NULLABLE) "true" else "false"
            val idGenerator = when (val kind = p.idGeneratorKind) {
                is IdGeneratorKind.Identity -> {
                    "$Identity<$entityTypeName, ${p.typeName}>(${p.typeName}::class)"
                }
                is IdGeneratorKind.Sequence -> {
                    val paramList = listOf(
                        "${p.typeName}::class",
                        "\"${kind.name}\"",
                        "${kind.incrementBy}",
                        "\"${kind.catalog}\"",
                        "\"${kind.schema}\""
                    ).joinToString(", ")
                    "$Sequence<$entityTypeName, ${p.typeName}>($paramList)"
                }
                else -> "null"
            }
            w.println("        val $p = $PropertyDescriptor<$entityTypeName, ${p.typeName}>(${p.typeName}::class, \"${p.column.name}\", { it.$p }, { e, v -> e.copy($p = v) }, $nullable, $idGenerator)")
        }
        w.println("    }")
    }

    private fun propertyMetamodels() {
        for (p in entity.properties) {
            w.println("    val $p by lazy { $PropertyMetamodelImpl(this, $EntityDescriptor.$p) }")
        }
    }

    private fun tableName() {
        w.println("    override fun tableName() = __tableName")
    }

    private fun catalogName() {
        w.println("    override fun catalogName() = __catalogName")
    }

    private fun schemaName() {
        w.println("    override fun schemaName() = __schemaName")
    }

    private fun idAssignment() {
        val p = entity.properties.firstOrNull { it.idGeneratorKind != null }
        w.print("    override fun idAssignment(): $Assignment<$entityTypeName>? = ")
        if (p != null) {
            w.println("$p.idAssignment")
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
        val nameList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { it.toString() }
        w.println("    override fun properties(): List<$PropertyMetamodel<$entityTypeName, *>> = listOf($nameList)")
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

    private fun companionObject() {
        w.println("    companion object {")
        w.println("        fun overrideTable(__tableName: String) = $simpleName(__tableName = __tableName)")
        w.println("        fun overrideCatalog(__catalogName: String) = $simpleName(__catalogName = __catalogName)")
        w.println("        fun overrideSchema(__schemaName: String) = $simpleName(__schemaName = __schemaName)")
        w.println("    }")
    }

    private fun utils() {
        if (entity.declaration.hasCompanionObject()) {
            w.println("")
            w.println("fun $entityTypeName.Companion.metamodel() = $simpleName()")
        }
    }
}

internal class EmptyEntityMetamodelGenerator(
    private val classDeclaration: KSClassDeclaration,
    private val packageName: String,
    private val simpleQualifiedName: String,
    private val fileName: String,
    private val w: PrintWriter
) : Runnable {
    override fun run() {
        w.println("package $packageName")
        w.println()
        w.println("@Suppress(\"ClassName\")")
        w.println("class $fileName : $EmptyEntityMetamodel<$simpleQualifiedName>() {")
        val parameters = classDeclaration.primaryConstructor?.parameters
        if (parameters != null) {
            for (p in parameters) {
                w.println("    val $p = $EmptyPropertyMetamodel<$simpleQualifiedName, ${p.type.resolve().declaration.qualifiedName?.asString()}>()")
            }
        }
        w.println("}")
        if (classDeclaration.hasCompanionObject()) {
            w.println("")
            w.println("fun $simpleQualifiedName.Companion.metamodel() = $fileName()")
        }
    }
}
