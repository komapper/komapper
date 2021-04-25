package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Nullability
import org.komapper.core.dsl.metamodel.EntityMetamodelImplementor
import java.io.PrintWriter
import java.time.ZonedDateTime

private const val Assignment = "org.komapper.core.dsl.metamodel.Assignment"
private const val EntityMetamodel = "org.komapper.core.dsl.metamodel.EntityMetamodel"
private const val EntityMetamodelStub = "org.komapper.core.dsl.metamodel.EntityMetamodelStub"
private const val EntityMetamodelImplementor = "org.komapper.core.dsl.metamodel.EntityMetamodelImplementor"
private const val AutoIncrement = "org.komapper.core.dsl.metamodel.Assignment.AutoIncrement"
private const val Sequence = "org.komapper.core.dsl.metamodel.Assignment.Sequence"
private const val PropertyDescriptor = "org.komapper.core.dsl.metamodel.PropertyDescriptor"
private const val PropertyMetamodel = "org.komapper.core.dsl.metamodel.PropertyMetamodel"
private const val PropertyMetamodelImpl = "org.komapper.core.dsl.metamodel.PropertyMetamodelImpl"
private const val PropertyMetamodelStub = "org.komapper.core.dsl.metamodel.PropertyMetamodelStub"
private const val Clock = "java.time.Clock"
private const val EntityDescriptor = "__EntityDescriptor"

internal class EntityMetamodelGenerator(
    private val entity: Entity,
    private val packageName: String,
    private val entityTypeName: String,
    private val simpleName: String,
    private val w: PrintWriter
) : Runnable {

    private val constructorParamList = listOf(
        "table: String = \"${entity.table.name}\"",
        "catalog: String = \"${entity.table.catalog}\"",
        "schema: String = \"${entity.table.schema}\"",
        "alwaysQuote: Boolean = ${entity.table.alwaysQuote}"
    ).joinToString(", ")

    private val idTypeName: String = if (entity.idProperties.size == 1) {
        entity.idProperties[0].typeName
    } else {
        "List<Any>"
    }

    override fun run() {
        w.println("package $packageName")
        w.println()
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@Suppress(\"ClassName\", \"PrivatePropertyName\")")
        w.println("@$EntityMetamodelImplementor")
        w.println("class $simpleName private constructor($constructorParamList) : $EntityMetamodel<$entityTypeName, $idTypeName, $simpleName> {")
        w.println("    private val __tableName = table")
        w.println("    private val __catalogName = catalog")
        w.println("    private val __schemaName = schema")
        w.println("    private val __alwaysQuote = alwaysQuote")

        entityDescriptor()

        propertyMetamodels()

        klass()
        tableName()
        catalogName()
        schemaName()
        alwaysQuote()

        idAssignment()
        idProperties()
        versionProperty()
        createdAtProperty()
        updatedAtProperty()
        properties()
        instantiate()
        getId()
        preInsert()
        preUpdate()
        postUpdate()

        newMetamodel()

        companionObject()

        w.println("}")

        utils()
    }

    private fun entityDescriptor() {
        w.println("    private object $EntityDescriptor {")
        for (p in entity.properties) {
            val columnName = "\"${p.column.name}\""
            val alwaysQuote = "${p.column.alwaysQuote}"
            val getter = "{ it.$p }"
            val setter = "{ e, v -> e.copy($p = v) }"
            val nullable = if (p.nullability == Nullability.NULLABLE) "true" else "false"
            val assignment = when (val kind = p.kind) {
                is PropertyKind.Id -> {
                    when (val idKind = kind.idKind) {
                        is IdKind.AutoIncrement -> {
                            "$AutoIncrement<$entityTypeName, ${p.typeName}>(${p.typeName}::class, $setter)"
                        }
                        is IdKind.Sequence -> {
                            val paramList = listOf(
                                "${p.typeName}::class",
                                setter,
                                "\"${idKind.name}\"",
                                "\"${idKind.catalog}\"",
                                "\"${idKind.schema}\"",
                                "${idKind.alwaysQuote}",
                                "${idKind.startWith}",
                                "${idKind.incrementBy}",
                            ).joinToString(", ")
                            "$Sequence<$entityTypeName, ${p.typeName}>($paramList)"
                        }
                        else -> "null"
                    }
                }
                else -> "null"
            }
            w.println("        val $p = $PropertyDescriptor<$entityTypeName, ${p.typeName}>(${p.typeName}::class, \"$p\", $columnName, $alwaysQuote, $getter, $setter, $nullable, $assignment)")
        }
        w.println("    }")
    }

    private fun propertyMetamodels() {
        for (p in entity.properties) {
            w.println("    val $p: $PropertyMetamodel<$entityTypeName, ${p.typeName}> by lazy { $PropertyMetamodelImpl(this, $EntityDescriptor.$p) }")
        }
    }

    private fun klass() {
        w.println("    override fun klass() = $entityTypeName::class")
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

    private fun alwaysQuote() {
        w.println("    override fun alwaysQuote() = __alwaysQuote")
    }

    private fun idAssignment() {
        val p = entity.properties.mapNotNull {
            when (it.kind) {
                is PropertyKind.Id ->
                    if (it.kind.idKind != null) it else null
                else -> null
            }
        }.firstOrNull()
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

    private fun createdAtProperty() {
        w.println("    override fun createdAtProperty(): $PropertyMetamodel<$entityTypeName, *>? = ${entity.createdAtProperty}")
    }

    private fun updatedAtProperty() {
        w.println("    override fun updatedAtProperty(): $PropertyMetamodel<$entityTypeName, *>? = ${entity.updatedAtProperty}")
    }

    private fun properties() {
        val nameList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { it.toString() }
        w.println("    override fun properties(): List<$PropertyMetamodel<$entityTypeName, *>> = listOf($nameList)")
    }

    private fun instantiate() {
        val argList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { p ->
            val nullability = if (p.nullability == Nullability.NULLABLE) "?" else ""
            "$p = m[this.$p] as ${p.typeName}$nullability"
        }
        w.println("    override fun instantiate(m: Map<$PropertyMetamodel<*, *>, Any?>) = $entityTypeName($argList)")
    }

    private fun getId() {
        val body = if (entity.idProperties.size == 1) {
            val p = entity.idProperties[0]
            val nullable = p.nullability == Nullability.NULLABLE
            "e.$p" + if (nullable) " ?: error(\"The id property '$p' must not null.\")" else ""
        } else {
            val list = entity.idProperties.joinToString {
                val nullable = it.nullability == Nullability.NULLABLE
                "e.$it" + if (nullable) " ?: error(\"The id property '$it' must not null.\")" else ""
            }
            "listOf($list)"
        }
        w.println("    override fun getId(e: $entityTypeName): $idTypeName = $body")
    }

    private fun preInsert() {
        val version = entity.versionProperty?.let {
            val nullable = it.nullability == Nullability.NULLABLE
            "$it = e.$it${if (nullable) " ?: 0" else ""}"
        }
        val createdAt = entity.createdAtProperty?.let {
            "$it = ${it.typeName}.now(c)"
        }
        val updatedAt = entity.updatedAtProperty?.let {
            "$it = ${it.typeName}.now(c)"
        }
        val paramList = listOfNotNull(version, createdAt, updatedAt).joinToString()
        val body = if (paramList == "") {
            "e"
        } else {
            "e.copy($paramList)"
        }
        w.println("    override fun preInsert(e: $entityTypeName, c: $Clock): $entityTypeName = $body")
    }

    private fun preUpdate() {
        val updatedAt = entity.updatedAtProperty?.let {
            "$it = ${it.typeName}.now(c)"
        }
        val body = if (updatedAt == null) {
            "e"
        } else {
            "e.copy($updatedAt)"
        }
        w.println("    override fun preUpdate(e: $entityTypeName, c: $Clock): $entityTypeName = $body")
    }

    private fun postUpdate() {
        val version = entity.versionProperty?.let {
            val nullable = it.nullability == Nullability.NULLABLE
            "$it = e.$it${if (nullable) "?" else ""}.inc()${if (nullable) " ?: 0" else ""}"
        }
        val body = if (version == null) {
            "e"
        } else {
            "e.copy($version)"
        }
        w.println("    override fun postUpdate(e: $entityTypeName): $entityTypeName = $body")
    }

    private fun newMetamodel() {
        val paramList = "table: String, catalog: String, schema: String, alwaysQuote: Boolean"
        w.println("    override fun newMetamodel($paramList) = $simpleName(table, catalog, schema, alwaysQuote)")
    }

    private fun companionObject() {
        w.println("    companion object {")
        w.println("        val alias = $simpleName()")
        w.println("        fun newAlias($constructorParamList) = $simpleName(table, catalog, schema, alwaysQuote)")
        w.println("    }")
    }

    private fun utils() {
        if (entity.declaration.hasCompanionObject()) {
            w.println("")
            w.println("val $entityTypeName.Companion.alias get() = $simpleName.alias")
            w.println("fun $entityTypeName.Companion.newAlias($constructorParamList) = $simpleName.newAlias(table, catalog, schema, alwaysQuote)")
        }
    }
}

internal class EntityMetamodelStubGenerator(
    private val classDeclaration: KSClassDeclaration,
    private val packageName: String,
    private val simpleQualifiedName: String,
    private val fileName: String,
    private val w: PrintWriter
) : Runnable {
    private val constructorParamList = listOf(
        "table: String = \"\"",
        "catalog: String = \"\"",
        "schema: String = \"\"",
        "alwaysQuote: Boolean = false"
    ).joinToString(", ")

    override fun run() {
        w.println("package $packageName")
        w.println()
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@Suppress(\"ClassName\")")
        w.println("@$EntityMetamodelImplementor")
        w.println("class $fileName : $EntityMetamodelStub<$simpleQualifiedName, $fileName>() {")
        val parameters = classDeclaration.primaryConstructor?.parameters
        if (parameters != null) {
            for (p in parameters) {
                val typeName = p.type.resolve().declaration.qualifiedName?.asString()
                w.println("    val $p: $PropertyMetamodel<$simpleQualifiedName, $typeName> = $PropertyMetamodelStub<$simpleQualifiedName, $typeName>()")
            }
        }
        w.println("    companion object {")
        w.println("        val alias = $fileName()")
        w.println("        fun newAlias($constructorParamList) = $fileName()")
        w.println("    }")
        w.println("}")
        if (classDeclaration.hasCompanionObject()) {
            w.println("")
            w.println("val $simpleQualifiedName.Companion.alias get() = $fileName.alias")
            w.println("fun $simpleQualifiedName.Companion.newAlias($constructorParamList) = $fileName.newAlias(table, catalog, schema, alwaysQuote)")
        }
    }
}
