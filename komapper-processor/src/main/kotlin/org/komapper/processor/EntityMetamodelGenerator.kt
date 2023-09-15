package org.komapper.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.Nullability
import org.komapper.processor.Symbols.Argument
import org.komapper.processor.Symbols.AutoIncrement
import org.komapper.processor.Symbols.Clock
import org.komapper.processor.Symbols.ConcurrentHashMap
import org.komapper.processor.Symbols.EmbeddableMetamodel
import org.komapper.processor.Symbols.EmbeddedMetamodel
import org.komapper.processor.Symbols.EntityDescriptor
import org.komapper.processor.Symbols.EntityMetamodel
import org.komapper.processor.Symbols.EntityMetamodelDeclaration
import org.komapper.processor.Symbols.EntityMetamodelFactory
import org.komapper.processor.Symbols.EntityMetamodelFactorySpi
import org.komapper.processor.Symbols.EntityMetamodelImplementor
import org.komapper.processor.Symbols.EntityStore
import org.komapper.processor.Symbols.EntityStoreContext
import org.komapper.processor.Symbols.EnumMappingException
import org.komapper.processor.Symbols.IdContext
import org.komapper.processor.Symbols.IdGenerator
import org.komapper.processor.Symbols.Instant
import org.komapper.processor.Symbols.KomapperExperimentalAssociation
import org.komapper.processor.Symbols.KotlinInstant
import org.komapper.processor.Symbols.KotlinLocalDateTime
import org.komapper.processor.Symbols.LocalDateTime
import org.komapper.processor.Symbols.Operand
import org.komapper.processor.Symbols.PropertyDescriptor
import org.komapper.processor.Symbols.PropertyMetamodel
import org.komapper.processor.Symbols.PropertyMetamodelImpl
import org.komapper.processor.Symbols.Sequence
import org.komapper.processor.Symbols.UUID
import org.komapper.processor.Symbols.checkMetamodelVersion
import org.komapper.processor.Symbols.toKotlinInstant
import org.komapper.processor.Symbols.toKotlinLocalDateTime
import java.io.PrintWriter
import java.time.ZonedDateTime

internal class EntityMetamodelGenerator(
    @Suppress("unused") private val logger: KSPLogger,
    private val config: Config,
    private val entity: Entity,
    private val unitTypeName: String,
    private val aliases: List<String>,
    private val packageName: String,
    private val simpleName: String,
    private val entityTypeName: String,
    private val w: PrintWriter,
) : Runnable {

    private val idTypeName: String = if (entity.idProperties.size == 1) {
        entity.idProperties[0].typeName
    } else {
        "List<Any>"
    }

    private val embeddableIndexMap: Map<Embeddable, Int> =
        entity.properties.filterIsInstance<CompositeProperty>().map { it.embeddable }
            .mapIndexed { index, embeddable -> embeddable to index }
            .toMap()

    private val constructorParamList = listOf(
        "table: String = \"${entity.table.name}\"",
        "catalog: String = \"${entity.table.catalog}\"",
        "schema: String = \"${entity.table.schema}\"",
        "alwaysQuote: Boolean = ${entity.table.alwaysQuote}",
        "disableSequenceAssignment: Boolean = false",
        "declaration: $EntityMetamodelDeclaration<$simpleName> = {}",
    ).joinToString(", ")

    override fun run() {
        w.println("@file:Suppress(\"ClassName\", \"PrivatePropertyName\", \"UNUSED_PARAMETER\", \"unused\", \"RemoveRedundantQualifierName\", \"MemberVisibilityCanBePrivate\", \"RedundantNullableReturnType\", \"USELESS_CAST\", \"UNCHECKED_CAST\", \"RemoveRedundantBackticks\", \"NO_EXPLICIT_VISIBILITY_IN_API_MODE\", \"NO_EXPLICIT_RETURN_TYPE_IN_API_MODE\", \"NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING\", \"NO_EXPLICIT_RETURN_TYPE_IN_API_MODE_WARNING\")")

        if (packageName.isNotEmpty()) {
            w.println("package $packageName")
            w.println()
        }

        importStatements()

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
        virtualIdProperties()
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

        metamodels()
        associations()
        aggregationRoot()
        factory()
    }

    private fun importStatements() {
        fun usesType(property: LeafProperty, typeName: String): Boolean {
            val propertyTypeName = when (val kotlinClass = property.kotlinClass) {
                is ValueClass -> kotlinClass.property.typeName
                else -> property.typeName
            }
            return propertyTypeName == typeName
        }

        val timestampProperties = listOf(entity.createdAtProperty, entity.updatedAtProperty)
        val usesInstant = timestampProperties.filterNotNull().any { usesType(it, KotlinInstant) }
        val usesLocalDateTime = timestampProperties.filterNotNull().any { usesType(it, KotlinLocalDateTime) }
        if (usesInstant || usesLocalDateTime) {
            if (usesInstant) w.println("import $toKotlinInstant")
            if (usesLocalDateTime) w.println("import $toKotlinLocalDateTime")
            w.println()
        }
    }

    private fun entityDescriptor() {
        fun leafPropertyDescriptor(p: LeafProperty, getter: String, setter: String, nullability: Nullability): String {
            val exteriorTypeName = p.exteriorTypeName
            val interiorTypeName = p.interiorTypeName
            val exteriorClass = "$exteriorTypeName::class"
            val interiorClass = "$interiorTypeName::class"
            val columnName = "\"${p.column.name}\""
            val alwaysQuote = "${p.column.alwaysQuote}"
            val masking = "${p.column.masking}"
            val wrap = when (p.kotlinClass) {
                is EnumClass -> {
                    fun throwException(value: String, propertyName: String, cause: String) =
                        "throw $EnumMappingException($exteriorClass, $propertyName, $value, $cause)"
                    when (val strategy = p.kotlinClass.strategy) {
                        is EnumStrategy.Name ->
                            "{ try { $exteriorTypeName.valueOf(it) } catch (e: IllegalArgumentException) { ${
                                throwException(
                                    "it",
                                    "\"${strategy.propertyName}\"",
                                    "e",
                                )
                            } } }"
                        is EnumStrategy.Ordinal ->
                            "{ try { $exteriorTypeName.values()[it] } catch (e: ArrayIndexOutOfBoundsException) { ${
                                throwException(
                                    "it",
                                    "\"${strategy.propertyName}\"",
                                    "e",
                                )
                            } } }"
                        is EnumStrategy.Property ->
                            "{ v -> $exteriorTypeName.values().firstOrNull { it.${strategy.propertyName} == v } ?: ${
                                throwException(
                                    "v",
                                    "\"${strategy.propertyName}\"",
                                    "null",
                                )
                            } }"
                        is EnumStrategy.Type ->
                            "{ it }"
                    }
                }
                is ValueClass -> {
                    val alternateType = p.kotlinClass.alternateType
                    if (alternateType != null) {
                        "{ ${p.kotlinClass}(it.${alternateType.property.declaration.simpleName.asString()}) }"
                    } else {
                        "{ ${p.kotlinClass}(it) }"
                    }
                }
                is PlainClass -> {
                    val alternateType = p.kotlinClass.alternateType
                    if (alternateType != null) {
                        "{ it.${alternateType.property.declaration.simpleName.asString()} }"
                    } else {
                        "{ it }"
                    }
                }
            }
            val unwrap = when (p.kotlinClass) {
                is EnumClass -> {
                    when (val strategy = p.kotlinClass.strategy) {
                        is EnumStrategy.Type -> "{ it }"
                        is EnumStrategy.Name -> "{ it.${strategy.propertyName} }"
                        is EnumStrategy.Ordinal -> "{ it.${strategy.propertyName} }"
                        is EnumStrategy.Property -> "{ it.${strategy.propertyName} }"
                    }
                }
                is ValueClass -> {
                    val alternateType = p.kotlinClass.alternateType
                    if (alternateType != null) {
                        "{ ${alternateType.exteriorTypeName}(it.${p.kotlinClass.property}) }"
                    } else {
                        "{ it.${p.kotlinClass.property} }"
                    }
                }
                is PlainClass -> {
                    val alternateType = p.kotlinClass.alternateType
                    if (alternateType != null) {
                        "{ ${alternateType.exteriorTypeName}(it) }"
                    } else {
                        "{ it }"
                    }
                }
            }
            val nullable = if (nullability == Nullability.NULLABLE) "true" else "false"
            val propertyDescriptor =
                "$PropertyDescriptor<$entityTypeName, $exteriorTypeName, $interiorTypeName>"
            return "$propertyDescriptor($exteriorClass, $interiorClass, \"$p\", $columnName, $alwaysQuote, $masking, $getter, $setter, $wrap, $unwrap, $nullable)"
        }

        w.println("    private object $EntityDescriptor {")
        val sequenceIdExists = entity.properties.filterIsInstance<LeafProperty>().any {
            when (it.kind) {
                is PropertyKind.Id -> it.kind.idKind is IdKind.Sequence
                else -> false
            }
        }
        if (sequenceIdExists) {
            w.println("        val __idContextMap: $ConcurrentHashMap<$UUID, $IdContext> = $ConcurrentHashMap()")
        }
        for (p in entity.properties) {
            when (p) {
                is CompositeProperty -> {
                    val nullable = p.nullability == Nullability.NULLABLE
                    val embeddable = p.embeddable
                    val argList =
                        embeddable.properties.joinToString(",\n            ", prefix = "\n            ") { ep ->
                            val getter = "{ it.`$p`${if (nullable) "?" else ""}.`$ep` }"
                            val setter = "{ e, v -> e.copy(`$p` = e.`$p`${if (nullable) "?" else ""}.copy(`$ep` = v)) }"
                            "`$ep` = ${leafPropertyDescriptor(ep, getter, setter, p.nullability)}"
                        }
                    val index = embeddableIndexMap[embeddable]
                    w.println("        val `$p` = __${embeddable.simpleName}$index($argList)")
                }
                is LeafProperty -> {
                    val getter = "{ it.`$p` }"
                    val setter = "{ e, v -> e.copy(`$p` = v) }"
                    w.println("        val `$p` = ${leafPropertyDescriptor(p, getter, setter, p.nullability)}")
                }
            }
        }
        for ((embeddable, index) in embeddableIndexMap) {
            val paramList = embeddable.properties.joinToString { ep ->
                "val `$ep`: $PropertyDescriptor<$entityTypeName, ${ep.exteriorTypeName}, ${ep.interiorTypeName}>"
            }
            w.println("        class __${embeddable.simpleName}$index($paramList)")
        }
        w.println("    }")
    }

    private fun propertyMetamodels() {
        for (p in entity.properties) {
            when (p) {
                is CompositeProperty -> {
                    val embeddable = p.embeddable
                    val argList = embeddable.properties.joinToString(",\n        ", prefix = "\n        ") { ep ->
                        "`$ep` = $PropertyMetamodelImpl(this, $EntityDescriptor.`$p`.`$ep`)"
                    }
                    val index = embeddableIndexMap[embeddable]
                    w.println("    val `$p`:  __${embeddable.simpleName}$index by lazy { __${embeddable.simpleName}$index($argList) }")
                }
                is LeafProperty -> {
                    val propertyMetamodel =
                        "$PropertyMetamodel<$entityTypeName, ${p.exteriorTypeName}, ${p.interiorTypeName}>"
                    w.println("    val `$p`: $propertyMetamodel by lazy { $PropertyMetamodelImpl(this, $EntityDescriptor.`$p`) }")
                }
            }
        }
        for ((embeddable, index) in embeddableIndexMap) {
            val paramList = embeddable.properties.joinToString { ep ->
                "val `$ep`: $PropertyMetamodel<$entityTypeName, ${ep.exteriorTypeName}, ${ep.interiorTypeName}>"
            }
            val propertyList = embeddable.properties.joinToString { "`$it`" }
            val argumentList = embeddable.properties.joinToString { ep -> "$Argument(this.`$ep`, composite?.`$ep`)" }
            w.println("    class __${embeddable.simpleName}$index($paramList): $EmbeddedMetamodel<$entityTypeName, ${embeddable.typeName}>, $EmbeddableMetamodel<${embeddable.typeName}> {")
            w.println("         override fun properties() = listOf($propertyList)")
            w.println("         override fun columns() = properties()")
            w.println("         override fun arguments(composite: ${embeddable.typeName}?) = listOf($argumentList)")
            w.println("    }")
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
        val pair = entity.properties.filterIsInstance<LeafProperty>().firstNotNullOfOrNull {
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
                    "$AutoIncrement(`$p`)"
                }
                is IdKind.Sequence -> {
                    val paramList = listOf(
                        "`$p`",
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
        val idNameList = if (entity.embeddedIdProperty != null) {
            val p = entity.embeddedIdProperty
            p.embeddable.properties.joinToString { "`$p`.`$it`" }
        } else {
            entity.idProperties.joinToString { "`$it`" }
        }
        w.println("    override fun idProperties(): List<$PropertyMetamodel<$entityTypeName, *, *>> = listOf($idNameList)")
    }

    private fun virtualIdProperties() {
        val idNameList = if (entity.virtualEmbeddedIdProperty != null) {
            val p = entity.virtualEmbeddedIdProperty
            p.embeddable.properties.joinToString { "`$p`.`$it`" }
        } else {
            entity.virtualIdProperties.joinToString { "`$it`" }
        }
        w.println("    override fun virtualIdProperties(): List<$PropertyMetamodel<$entityTypeName, *, *>> = listOf($idNameList)")
    }

    private fun versionProperty() {
        val body = if (entity.versionProperty == null) "null" else "`${entity.versionProperty}`"
        w.println("    override fun versionProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = $body")
    }

    private fun createdAtProperty() {
        val body = if (entity.createdAtProperty == null) "null" else "`${entity.createdAtProperty}`"
        w.println("    override fun createdAtProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = $body")
    }

    private fun updatedAtProperty() {
        val body = if (entity.updatedAtProperty == null) "null" else "`${entity.updatedAtProperty}`"
        w.println("    override fun updatedAtProperty(): $PropertyMetamodel<$entityTypeName, *, *>? = $body")
    }

    private fun properties() {
        val properties = entity.properties.flatMap { p ->
            when (p) {
                is CompositeProperty -> p.embeddable.properties.map { "`$p`.`$it`" }
                is LeafProperty -> listOf("`$p`")
            }
        }
        val nameList = properties.joinToString(",\n        ", prefix = "\n        ")
        w.println("    override fun properties(): List<$PropertyMetamodel<$entityTypeName, *, *>> = listOf($nameList)")
    }

    private fun extractId() {
        val body = if (entity.embeddedIdProperty != null) {
            val p = entity.embeddedIdProperty
            val list = p.embeddable.properties.joinToString { ep ->
                val nullable1 = p.nullability == Nullability.NULLABLE
                val nullable2 = ep.nullability == Nullability.NULLABLE
                "e.`$p`${if (nullable1) "?" else ""}.`$ep`" + if (nullable1 || nullable2) " ?: error(\"The id property '$p.$ep' must not null.\")" else ""
            }
            "listOf($list)"
        } else if (entity.idProperties.size == 1) {
            val p = entity.idProperties[0]
            val nullable = p.nullability == Nullability.NULLABLE
            "e.`$p`" + if (nullable) " ?: error(\"The id property '$p' must not null.\")" else ""
        } else {
            val list = entity.idProperties.joinToString {
                val nullable = it.nullability == Nullability.NULLABLE
                "e.`$it`" + if (nullable) " ?: error(\"The id property '$it' must not null.\")" else ""
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
            if (id == null) "null" else "this.`$p`.wrap($id)"
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
                    "`$it` to $Argument(`$it`, ${it.kotlinClass}(0$tag))"
                }
                else -> {
                    val tag = it.literalTag
                    "`$it` to $Argument(`$it`, 0$tag)"
                }
            }
        } ?: "null"
        w.println("    override fun versionAssignment(): Pair<$PropertyMetamodel<$entityTypeName, *, *>, $Operand>? = $body")
    }

    private fun createdAtAssignment() {
        val body = entity.createdAtProperty?.let {
            "`$it` to $Argument(`$it`, ${now(it)})"
        } ?: "null"
        w.println("    override fun createdAtAssignment(c: $Clock): Pair<$PropertyMetamodel<$entityTypeName, *, *>, $Operand>? = $body")
    }

    private fun updatedAtAssignment() {
        val body = entity.updatedAtProperty?.let {
            "`$it` to $Argument(`$it`, ${now(it)})"
        } ?: "null"
        w.println("    override fun updatedAtAssignment(c: $Clock): Pair<$PropertyMetamodel<$entityTypeName, *, *>, $Operand>? = $body")
    }

    private fun preInsert() {
        val version = entity.versionProperty?.let {
            val nullable = it.nullability == Nullability.NULLABLE
            when (it.kotlinClass) {
                is ValueClass -> {
                    val tag = it.kotlinClass.property.literalTag
                    "`$it` = e.`$it`${if (nullable) " ?: ${it.kotlinClass}(0$tag)" else ""}"
                }
                else -> {
                    val tag = it.literalTag
                    "`$it` = e.`$it`${if (nullable) " ?: 0$tag" else ""}"
                }
            }
        }
        val createdAt = entity.createdAtProperty?.let { "`$it` = ${now(it)}" }
        val updatedAt = entity.updatedAtProperty?.let { "`$it` = ${now(it)}" }
        val paramList = listOfNotNull(version, createdAt, updatedAt).joinToString()
        val body = if (paramList == "") {
            "e"
        } else {
            "e.copy($paramList)"
        }
        w.println("    override fun preInsert(e: $entityTypeName, c: $Clock): $entityTypeName = $body")
    }

    private fun preUpdate() {
        val updatedAt = entity.updatedAtProperty?.let { "`$it` = ${now(it)}" }
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
                    "`$it` = ${it.kotlinClass}(e.`$it`${if (nullable) "?" else ""}.${it.kotlinClass.property}${if (nullable) "?" else ""}.inc()${if (nullable) " ?: 0$tag" else ""})"
                }
                else -> {
                    val tag = it.literalTag
                    "`$it` = e.`$it`${if (nullable) "?" else ""}.inc()${if (nullable) " ?: 0$tag" else ""}"
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

    private fun now(property: LeafProperty): String {
        return when (property.kotlinClass) {
            is ValueClass -> {
                when (property.kotlinClass.property.typeName) {
                    KotlinInstant -> {
                        "${property.typeName}($Instant.now(c).${toKotlinInstant.split(".").last()}())"
                    }
                    KotlinLocalDateTime -> {
                        "${property.typeName}($LocalDateTime.now(c).${toKotlinLocalDateTime.split(".").last()}())"
                    }
                    else -> {
                        "${property.typeName}(${property.kotlinClass.property.typeName}.now(c))"
                    }
                }
            }
            else -> {
                when (property.typeName) {
                    KotlinInstant -> {
                        "$Instant.now(c).${toKotlinInstant.split(".").last()}()"
                    }
                    KotlinLocalDateTime -> {
                        "$LocalDateTime.now(c).${toKotlinLocalDateTime.split(".").last()}()"
                    }
                    else -> {
                        "${property.typeName}.now(c)"
                    }
                }
            }
        }
    }

    private fun newEntity() {
        val argList = entity.properties.joinToString(",\n        ", prefix = "\n        ") { p ->
            when (p) {
                is CompositeProperty -> {
                    val args =
                        p.embeddable.properties.joinToString(",\n            ", prefix = "\n            ") { ep ->
                            val nullable = ep.nullability == Nullability.NULLABLE
                            "`$ep` = m[this.`$p`.`$ep`] as ${ep.typeName}${if (nullable) "?" else ""}"
                        }
                    if (p.nullability == Nullability.NULLABLE) {
                        val condition = p.embeddable.properties.joinToString(" && ") { ep ->
                            "m[this.`$p`.`$ep`] == null"
                        }
                        "`$p` = if ($condition) null else ${p.embeddable.typeName}($args)"
                    } else {
                        "`$p` = ${p.embeddable.typeName}($args)"
                    }
                }
                is LeafProperty -> {
                    val nullability = if (p.nullability == Nullability.NULLABLE) "?" else ""
                    "`$p` = m[this.`$p`] as ${p.typeName}$nullability"
                }
            }
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
        w.println("            $checkMetamodelVersion(\"$packageName.$simpleName\", ${org.komapper.core.dsl.metamodel.EntityMetamodel.METAMODEL_VERSION})")
        w.println("        }")
        for (alias in aliases) {
            w.println("        val `$alias` = $simpleName()")
        }
        w.println("    }")
    }

    private fun metamodels() {
        for (alias in aliases) {
            w.println("val $unitTypeName.`$alias` get() = $simpleName.`$alias`")
        }
        w.println()
    }

    private fun associations() {
        for (association in entity.associations) {
            val sourceEntity = association.sourceEntity
            val targetEntity = association.targetEntity
            val returnType = when (association.kind) {
                AssociationKind.ONE_TO_ONE,
                AssociationKind.MANY_TO_ONE,
                -> "${targetEntity.typeName}?"
                AssociationKind.ONE_TO_MANY -> "Set<${targetEntity.typeName}>"
            }
            val expression = when (association.kind) {
                AssociationKind.ONE_TO_ONE -> "store.findOne(source, target, this)"
                AssociationKind.MANY_TO_ONE -> "store.findOne(source, target, this)"
                AssociationKind.ONE_TO_MANY -> "store.findMany(source, target, this)"
            }
            w.println(
                """
                @$KomapperExperimentalAssociation
                fun $entityTypeName.`${association.navigator}`(
                    store: $EntityStore,
                    source: ${sourceEntity.packageName}.${sourceEntity.metamodelSimpleName} = ${sourceEntity.unitTypeName}.`${association.link.source}`,
                    target: ${targetEntity.packageName}.${targetEntity.metamodelSimpleName} = ${targetEntity.unitTypeName}.`${association.link.target}`,
                    ): $returnType {
                """.trimIndent(),
            )
            w.println("    return $expression")
            w.println("}")
            w.println()
            if (config.enableEntityStoreContext) {
                w.println(
                    """
                context($EntityStoreContext)
                @$KomapperExperimentalAssociation
                fun $entityTypeName.`${association.navigator}`(
                    source: ${sourceEntity.packageName}.${sourceEntity.metamodelSimpleName} = ${sourceEntity.unitTypeName}.`${association.link.source}`,
                    target: ${targetEntity.packageName}.${targetEntity.metamodelSimpleName} = ${targetEntity.unitTypeName}.`${association.link.target}`,
                    ): $returnType {
                    """.trimIndent(),
                )
                w.println("    return $expression")
                w.println("}")
                w.println()
            }
        }
    }

    private fun aggregationRoot() {
        val aggregateRoot = entity.aggregateRoot ?: return
        val targetEntity = aggregateRoot.targetEntity
        w.println(
            """
                @$KomapperExperimentalAssociation
                fun $EntityStore.`${aggregateRoot.navigator}`(
                    target: ${targetEntity.packageName}.${targetEntity.metamodelSimpleName} = ${targetEntity.unitTypeName}.`${aggregateRoot.target}`,
                    ): Set<${targetEntity.typeName}> {
            """.trimIndent(),
        )
        w.println("    return this[target]")
        w.println("}")
        w.println()
        if (config.enableEntityStoreContext) {
            w.println(
                """
                context($EntityStoreContext)
                @$KomapperExperimentalAssociation
                fun `${aggregateRoot.navigator}`(
                    target: ${targetEntity.packageName}.${targetEntity.metamodelSimpleName} = ${targetEntity.unitTypeName}.`${aggregateRoot.target}`,
                    ): Set<${targetEntity.typeName}> {
                """.trimIndent(),
            )
            w.println("    return store[target]")
            w.println("}")
            w.println()
        }
    }

    private fun factory() {
        w.println("@$EntityMetamodelFactory")
        w.println("class ${simpleName}_Factory: $EntityMetamodelFactorySpi {")
        w.println("    override fun create() = listOf(${aliases.joinToString { "$unitTypeName to $simpleName.`$it`" }})")
        w.println("}")
        w.println()
    }
}
