package org.komapper.processor

import com.google.devtools.ksp.symbol.Nullability
import org.komapper.processor.Symbols.Argument
import org.komapper.processor.Symbols.AutoIncrement
import org.komapper.processor.Symbols.Clock
import org.komapper.processor.Symbols.ConcurrentHashMap
import org.komapper.processor.Symbols.EntityDescriptor
import org.komapper.processor.Symbols.EntityMetamodel
import org.komapper.processor.Symbols.EntityMetamodelDeclaration
import org.komapper.processor.Symbols.EntityMetamodelImplementor
import org.komapper.processor.Symbols.IdContext
import org.komapper.processor.Symbols.IdGenerator
import org.komapper.processor.Symbols.Operand
import org.komapper.processor.Symbols.PropertyDescriptor
import org.komapper.processor.Symbols.PropertyMetamodel
import org.komapper.processor.Symbols.PropertyMetamodelImpl
import org.komapper.processor.Symbols.Sequence
import org.komapper.processor.Symbols.UUID
import org.komapper.processor.Symbols.checkMetamodelVersion
import java.io.PrintWriter
import java.time.ZonedDateTime

internal class EntityMetamodelGenerator(
    private val entity: Entity,
    private val metaObject: String,
    private val aliases: List<String>,
    private val packageName: String,
    private val simpleName: String,
    private val entityTypeName: String,
    private val w: PrintWriter
) : Runnable {

    private val idTypeName: String = if (entity.idProperties.size == 1) {
        entity.idProperties[0].typeName
    } else {
        "List<Any>"
    }

    private val constructorParamList = listOf(
        "table: String = \"${entity.table.name}\"",
        "catalog: String = \"${entity.table.catalog}\"",
        "schema: String = \"${entity.table.schema}\"",
        "alwaysQuote: Boolean = ${entity.table.alwaysQuote}",
        "disableSequenceAssignment: Boolean = false",
        "declaration: $EntityMetamodelDeclaration<$simpleName> = {}"
    ).joinToString(", ")

    override fun run() {
        w.println("@file:Suppress(\"ClassName\", \"PrivatePropertyName\", \"UNUSED_PARAMETER\", \"unused\", \"RemoveRedundantQualifierName\", \"MemberVisibilityCanBePrivate\", \"RedundantNullableReturnType\")")

        if (packageName.isNotEmpty()) {
            w.println("package $packageName")
            w.println()
        }
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@$EntityMetamodelImplementor($entityTypeName::class)")
        w.println("class $simpleName private constructor($constructorParamList) : $EntityMetamodel<$entityTypeName, $idTypeName, $simpleName> {")
        w.println("    private val __tableName = table")
        w.println("    private val __catalogName = catalog")
        w.println("    private val __schemaName = schema")
        w.println("    private val __alwaysQuote = alwaysQuote")
        w.println("    private val __disableSequenceAssignment = disableSequenceAssignment")
        w.println("    private val __declaration = declaration")

        entityDescriptor()

        propertyMetamodels()

        klass()
        tableName()
        catalogName()
        schemaName()
        alwaysQuote()
        disableSequenceAssignment()
        declaration()

        idGenerator()
        idProperties()
        versionProperty()
        createdAtProperty()
        updatedAtProperty()
        properties()
        extractId()
        convertToId()
        versionAssignment()
        createdAtAssignment()
        updatedAtAssignment()
        preInsert()
        preUpdate()
        postUpdate()

        newEntity()
        newMetamodel()
        clone()

        companionObject()

        w.println("}")
        w.println()

        utils()
    }

    private fun entityDescriptor() {
        w.println("    private object $EntityDescriptor {")
        val sequenceIdExists = entity.properties.any {
            when (it.kind) {
                is PropertyKind.Id -> it.kind.idKind is IdKind.Sequence
                else -> false
            }
        }
        if (sequenceIdExists) {
            w.println("        val __idContextMap: $ConcurrentHashMap<$UUID, $IdContext> = $ConcurrentHashMap()")
        }
        for (p in entity.properties) {
            val exteriorTypeName = p.exteriorTypeName
            val interiorTypeName = p.interiorTypeName
            val exteriorClass = "$exteriorTypeName::class"
            val interiorClass = "$interiorTypeName::class"
            val columnName = "\"${p.column.name}\""
            val alwaysQuote = "${p.column.alwaysQuote}"
            val masking = "${p.column.masking}"
            val getter = "{ it.$p }"
            val setter = "{ e, v -> e.copy($p = v) }"
            val wrap = when (p.kotlinClass) {
                is EnumClass -> "{ ${p.kotlinClass.declaration}.valueOf(it) }"
                is ValueClass -> "{ ${p.kotlinClass}(it) }"
                else -> "{ it }"
            }
            val unwrap = when (p.kotlinClass) {
                is EnumClass -> "{ it.toString() }"
                is ValueClass -> "{ it.${p.kotlinClass.property} }"
                else -> "{ it }"
            }
            val nullable = if (p.nullability == Nullability.NULLABLE) "true" else "false"
            val propertyDescriptor =
                "$PropertyDescriptor<$entityTypeName, $exteriorTypeName, $interiorTypeName>"
            w.println("        val $p = $propertyDescriptor($exteriorClass, $interiorClass, \"$p\", $columnName, $alwaysQuote, $masking, $getter, $setter, $wrap, $unwrap, $nullable)")
        }
        w.println("    }")
    }

    private fun propertyMetamodels() {
        for (p in entity.properties) {
            val exteriorTypeName = p.exteriorTypeName
            val interiorTypeName = p.interiorTypeName
            val propertyMetamodel =
                "$PropertyMetamodel<$entityTypeName, $exteriorTypeName, $interiorTypeName>"
            w.println("    val $p: $propertyMetamodel by lazy { $PropertyMetamodelImpl(this, $EntityDescriptor.$p) }")
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

    private fun disableSequenceAssignment() {
        w.println("    override fun disableSequenceAssignment() = __disableSequenceAssignment")
    }

    private fun declaration() {
        w.println("    override fun declaration() = __declaration")
    }

    private fun idGenerator() {
        val pair = entity.properties.firstNotNullOfOrNull {
            when (it.kind) {
                is PropertyKind.Id -> {
                    val idKind = it.kind.idKind
                    if (idKind != null) it to idKind else null
                }
                else -> null
            }
        }

        w.print("    override fun idGenerator(): $IdGenerator<$entityTypeName, $idTypeName>? = ")
        if (pair != null) {
            val (p, idKind) = pair
            val assignment = when (idKind) {
                is IdKind.AutoIncrement -> {
                    "$AutoIncrement($p)"
                }
                is IdKind.Sequence -> {
                    val paramList = listOf(
                        "$p",
                        "::convertToId",
                        "$EntityDescriptor.__idContextMap",
                        "\"${idKind.name}\"",
                        "\"${idKind.catalog}\"",
                        "\"${idKind.schema}\"",
                        "${idKind.alwaysQuote}",
                        "${idKind.startWith}",
                        "${idKind.incrementBy}",
                    ).joinToString(", ")
                    "$Sequence($paramList)"
                }
            }
            w.println(assignment)
        } else {
            w.println("null")
        }
    }

    private fun idProperties() {
        val idNameList = entity.idProperties.joinToString { it.toString() }
        w.println("    override fun idProperties(): List<$PropertyMetamodel<$entityTypeName, *, *>> = listOf($idNameList)")
    }

    private fun versionProperty() {
        w.println("    override fun versionProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = ${entity.versionProperty}")
    }

    private fun createdAtProperty() {
        w.println("    override fun createdAtProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = ${entity.createdAtProperty}")
    }

    private fun updatedAtProperty() {
        w.println("    override fun updatedAtProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = ${entity.updatedAtProperty}")
    }

    private fun properties() {
        val nameList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { it.toString() }
        w.println("    override fun properties(): List<$PropertyMetamodel<$entityTypeName, *, *>> = listOf($nameList)")
    }

    private fun extractId() {
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
        w.println("    override fun extractId(e: $entityTypeName): $idTypeName = $body")
    }

    private fun convertToId() {
        val body = if (entity.idProperties.size == 1) {
            val p = entity.idProperties[0]
            val typeName = when (p.kotlinClass) {
                is ValueClass -> p.kotlinClass.property.typeName
                else -> p.typeName
            }
            val id = when (typeName) {
                "kotlin.Int" -> "generatedKey.toInt()"
                "kotlin.Long" -> "generatedKey"
                "kotlin.UInt" -> "generatedKey.toUInt()"
                else -> null
            }
            if (id == null) "null" else "this.$p.wrap($id)"
        } else {
            "null"
        }
        w.println("    override fun convertToId(generatedKey: Long): $idTypeName? = $body")
    }

    private fun versionAssignment() {
        val body = entity.versionProperty?.let {
            when (it.kotlinClass) {
                is ValueClass -> {
                    val tag = it.kotlinClass.property.literalTag
                    "$it to $Argument($it, ${it.kotlinClass}(0$tag))"
                }
                else -> {
                    val tag = it.literalTag
                    "$it to $Argument($it, 0$tag)"
                }
            }
        } ?: "null"
        w.println("    override fun versionAssignment(): Pair<$PropertyMetamodel<$entityTypeName, *, *>, $Operand>? = $body")
    }

    private fun createdAtAssignment() {
        val body = entity.createdAtProperty?.let {
            when (it.kotlinClass) {
                is ValueClass -> {
                    "$it to $Argument($it, ${it.typeName}(${it.kotlinClass.property.typeName}.now(c)))"
                }
                else -> {
                    "$it to $Argument($it, ${it.typeName}.now(c))"
                }
            }
        } ?: "null"
        w.println("    override fun createdAtAssignment(c: $Clock): Pair<$PropertyMetamodel<$entityTypeName, *, *>, $Operand>? = $body")
    }

    private fun updatedAtAssignment() {
        val body = entity.updatedAtProperty?.let {
            when (it.kotlinClass) {
                is ValueClass -> {
                    "$it to $Argument($it, ${it.typeName}(${it.kotlinClass.property.typeName}.now(c)))"
                }
                else -> {
                    "$it to $Argument($it, ${it.typeName}.now(c))"
                }
            }
        } ?: "null"
        w.println("    override fun updatedAtAssignment(c: $Clock): Pair<$PropertyMetamodel<$entityTypeName, *, *>, $Operand>? = $body")
    }

    private fun preInsert() {
        val version = entity.versionProperty?.let {
            val nullable = it.nullability == Nullability.NULLABLE
            when (it.kotlinClass) {
                is ValueClass -> {
                    val tag = it.kotlinClass.property.literalTag
                    "$it = e.$it${if (nullable) " ?: ${it.kotlinClass}(0$tag)" else ""}"
                }
                else -> {
                    val tag = it.literalTag
                    "$it = e.$it${if (nullable) " ?: 0$tag" else ""}"
                }
            }
        }
        val createdAt = entity.createdAtProperty?.let {
            when (it.kotlinClass) {
                is ValueClass -> {
                    "$it = ${it.typeName}(${it.kotlinClass.property.typeName}.now(c))"
                }
                else -> {
                    "$it = ${it.typeName}.now(c)"
                }
            }
        }
        val updatedAt = entity.updatedAtProperty?.let {
            when (it.kotlinClass) {
                is ValueClass -> {
                    "$it = ${it.typeName}(${it.kotlinClass.property.typeName}.now(c))"
                }
                else -> {
                    "$it = ${it.typeName}.now(c)"
                }
            }
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
            when (it.kotlinClass) {
                is ValueClass -> {
                    "$it = ${it.typeName}(${it.kotlinClass.property.typeName}.now(c))"
                }
                else -> {
                    "$it = ${it.typeName}.now(c)"
                }
            }
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
            when (it.kotlinClass) {
                is ValueClass -> {
                    val tag = it.kotlinClass.property.literalTag
                    "$it = ${it.kotlinClass}(e.$it${if (nullable) "?" else ""}.${it.kotlinClass.property}${if (nullable) "?" else ""}.inc()${if (nullable) " ?: 0$tag" else ""})"
                }
                else -> {
                    val tag = it.literalTag
                    "$it = e.$it${if (nullable) "?" else ""}.inc()${if (nullable) " ?: 0$tag" else ""}"
                }
            }
        }
        val body = if (version == null) {
            "e"
        } else {
            "e.copy($version)"
        }
        w.println("    override fun postUpdate(e: $entityTypeName): $entityTypeName = $body")
    }

    private fun newEntity() {
        val argList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { p ->
            val nullability = if (p.nullability == Nullability.NULLABLE) "?" else ""
            "$p = m[this.$p] as ${p.typeName}$nullability"
        }
        w.println("    override fun newEntity(m: Map<$PropertyMetamodel<*, *, *>, Any?>) = $entityTypeName($argList)")
    }

    private fun newMetamodel() {
        val paramList =
            "table: String, catalog: String, schema: String, alwaysQuote: Boolean, disableSequenceAssignment: Boolean, declaration: $EntityMetamodelDeclaration<$simpleName>"
        w.println("    override fun newMetamodel($paramList) = $simpleName(table, catalog, schema, alwaysQuote, disableSequenceAssignment, declaration)")
    }

    private fun clone() {
        w.println("    fun clone($constructorParamList) = $simpleName(table, catalog, schema, alwaysQuote, disableSequenceAssignment, declaration)")
    }

    private fun companionObject() {
        w.println("    companion object {")
        w.println("        init {")
        w.println("            $checkMetamodelVersion(\"$packageName.$simpleName\", $EntityMetamodel.METAMODEL_VERSION)")
        w.println("        }")
        for (alias in aliases) {
            w.println("        val $alias = $simpleName()")
        }
        w.println("    }")
    }

    private fun utils() {
        for (alias in aliases) {
            w.println("val $metaObject.$alias get() = $simpleName.$alias")
        }
    }
}
