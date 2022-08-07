package org.komapper.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperColumnOverride
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperEnumOverride
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperIgnore
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.core.NamingStrategy

internal class EntityDefFactory(
    @Suppress("unused") private val logger: KSPLogger,
    @Suppress("unused") private val config: Config,
    private val definitionSource: EntityDefinitionSource
) {
    private val namingStrategy: NamingStrategy = config.namingStrategy
    private val defDeclaration = definitionSource.defDeclaration

    fun create(): EntityDef {
        val table = getTable()
        val allProperties = createAllProperties()
        val compositeProperties = allProperties.filterIsInstance<CompositePropertyDef>()
        val leafProperties = allProperties.filterIsInstance<LeafPropertyDef>()
        validateCompositeProperties(compositeProperties)
        validateLeafProperties(leafProperties)
        return EntityDef(definitionSource, table, allProperties)
    }

    private fun getTable(): Table {
        val annotation = defDeclaration.findAnnotation(KomapperTable::class)
        val name = annotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(definitionSource.entityDeclaration.simpleName.asString())
        val catalog = annotation?.findValue("catalog")?.toString()?.trim() ?: KomapperTable.CATALOG
        val schema = annotation?.findValue("schema")?.toString()?.trim() ?: KomapperTable.SCHEMA
        val alwaysQuote =
            annotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: KomapperTable.ALWAYS_QUOTE
        return Table(name, catalog, schema, alwaysQuote)
    }

    private fun getColumn(parameter: KSValueParameter): Column {
        val annotation = parameter.findAnnotation(KomapperColumn::class)
        return getColumn(annotation, parameter.toString())
    }

    private fun getColumn(columnAnnotation: KSAnnotation?, propertyName: String): Column {
        val name = columnAnnotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(propertyName)
        val alwaysQuote =
            columnAnnotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: KomapperColumn.ALWAYS_QUOTE
        val masking =
            columnAnnotation?.findValue("masking")?.toString()?.toBooleanStrict() ?: KomapperColumn.MASKING
        return Column(name, alwaysQuote, masking)
    }

    private fun getColumns(parameter: KSValueParameter): List<Triple<String, Column, KSAnnotation>> {
        return parameter.annotations
            .filter { it.shortName.asString() == KomapperColumnOverride::class.simpleName }
            .map {
                val name = it.findValue("name")?.toString()
                val columnNode = it.findValue("column") as? KSNode
                val columnAnnotation = columnNode?.accept(AnnotationVisitor(), Unit)
                Triple(name, columnAnnotation, it)
            }.filter {
                it.first != null && it.second != null
            }.map {
                val column = getColumn(it.second!!, it.first!!)
                Triple(it.first!!, column, it.third)
            }.toList()
    }

    private fun getEnumStrategies(parameter: KSValueParameter): List<Triple<String, EnumStrategy, KSAnnotation>> {
        return parameter.annotations
            .filter { it.shortName.asString() == KomapperEnumOverride::class.simpleName }
            .map {
                val name = it.findValue("name")?.toString()
                val enumNode = it.findValue("enum") as? KSNode
                val enumAnnotation = enumNode?.accept(AnnotationVisitor(), Unit)
                Triple(name, enumAnnotation, it)
            }.filter {
                it.first != null && it.second != null
            }.map {
                val enumStrategy = getEnumStrategy(it.second)
                Triple(it.first!!, enumStrategy, it.third)
            }.toList()
    }

    private fun createAllProperties(): List<PropertyDef> {
        val propertyDeclarationMap = defDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        val parameters = defDeclaration.primaryConstructor?.parameters ?: return emptyList()
        return parameters.map { parameter ->
            val declaration = propertyDeclarationMap[parameter.name]
                ?: report("The corresponding property declaration is not found.", parameter)
            when (val kind = createPropertyKind(parameter, null)) {
                is PropertyKind.Embedded, is PropertyKind.EmbeddedId ->
                    createCompositeProperty(parameter, declaration, kind)
                else ->
                    createLeafProperty(parameter, declaration)
            }
        }
    }

    private fun createCompositeProperty(
        parameter: KSValueParameter,
        declaration: KSPropertyDeclaration,
        kind: PropertyKind
    ): CompositePropertyDef {
        val resolvedDeclaration = declaration.type.resolve().declaration
        val embeddableDeclaration = resolvedDeclaration.accept(ClassDeclarationVisitor(), Unit) ?: report(
            "@${kind.annotation.shortName} cannot be applied to this element. " +
                "${resolvedDeclaration.simpleName.asString()} must be a data class.",
            declaration
        )
        validateContainerClass(embeddableDeclaration, kind.annotation)
        val columns = getColumns(parameter)
        val columnMap = columns.associate { it.first to it.second }
        val enumStrategies = getEnumStrategies(parameter)
        val enumStrategyMap = enumStrategies.associate { it.first to it.second }
        val leafProperties = createLeafProperties(embeddableDeclaration, columnMap, enumStrategyMap)
        val propertyNames = leafProperties.map { it.parameter.name?.asString() }.toSet()
        fun checkPropertyName(name: String, annotation: KSAnnotation) {
            if (name !in propertyNames) {
                report(
                    "The property \"$name\" is not found in the class \"${embeddableDeclaration.simpleName.asString()}\".",
                    annotation
                )
            }
        }
        columns.forEach { checkPropertyName(it.first, it.third) }
        enumStrategies.forEach { checkPropertyName(it.first, it.third) }
        val embeddableDef = EmbeddableDef(embeddableDeclaration, leafProperties)
        return CompositePropertyDef(parameter, declaration, kind, embeddableDef)
    }

    private fun createLeafProperties(
        classDeclaration: KSClassDeclaration,
        columnMap: Map<String, Column>,
        enumStrategyMap: Map<String, EnumStrategy>
    ): List<LeafPropertyDef> {
        val propertyDeclarationMap = classDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        val parameters = classDeclaration.primaryConstructor?.parameters ?: return emptyList()
        return parameters.map { parameter ->
            val declaration = propertyDeclarationMap[parameter.name]
                ?: report("The corresponding property declaration is not found.", parameter)
            val name = parameter.name?.asString()
            val column = columnMap[name] ?: getColumn(parameter)
            val enumStrategy = enumStrategyMap[name]
            LeafPropertyDef(parameter, declaration, null, column, enumStrategy)
        }
    }

    private fun createLeafProperty(parameter: KSValueParameter, declaration: KSPropertyDeclaration): LeafPropertyDef {
        val idKind = createIdKind(parameter)
        val kind = createPropertyKind(parameter, idKind)
        val column = getColumn(parameter)
        val enumStrategy = createEnumStrategy(parameter)
        return LeafPropertyDef(parameter, declaration, kind, column, enumStrategy)
    }

    private fun validateCompositeProperties(properties: List<CompositePropertyDef>) {
        if (properties.hasDuplicates { it.kind is PropertyKind.EmbeddedId }) {
            report(
                "Multiple @${KomapperEmbeddedId::class.simpleName} cannot coexist in a single class.",
                defDeclaration
            )
        }
    }

    private fun validateLeafProperties(properties: List<LeafPropertyDef>) {
        if (properties.hasDuplicates { it.kind is PropertyKind.Version }) {
            report("Multiple @${KomapperVersion::class.simpleName} cannot coexist in a single class.", defDeclaration)
        }
        if (properties.hasDuplicates { it.kind is PropertyKind.CreatedAt }) {
            report("Multiple @${KomapperCreatedAt::class.simpleName} cannot coexist in a single class.", defDeclaration)
        }
        if (properties.hasDuplicates { it.kind is PropertyKind.UpdatedAt }) {
            report("Multiple @${KomapperUpdatedAt::class.simpleName} cannot coexist in a single class.", defDeclaration)
        }
        val idKinds = properties.mapNotNull {
            when (it.kind) {
                is PropertyKind.Id -> it.kind.idKind
                else -> null
            }
        }
        if (idKinds.count() > 1) {
            report(
                "@${KomapperAutoIncrement::class.simpleName} and @${KomapperSequence::class.simpleName} cannot coexist in a single class.",
                defDeclaration
            )
        }
    }

    private fun createIdKind(parameter: KSValueParameter): IdKind? {
        var autoIncrement: IdKind.AutoIncrement? = null
        var sequence: IdKind.Sequence? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                KomapperAutoIncrement::class.simpleName -> autoIncrement = IdKind.AutoIncrement(a)
                KomapperSequence::class.simpleName -> sequence = let {
                    val name = a.findValue("name")?.toString()?.trim()
                        ?: report("@${KomapperSequence::class.simpleName}.name is not found.", a)
                    val startWith = a.findValue("startWith") ?: KomapperSequence.START_WITH
                    val incrementBy = a.findValue("incrementBy") ?: KomapperSequence.INCREMENT_BY
                    val catalog = a.findValue("catalog")?.toString()?.trim() ?: KomapperSequence.CATALOG
                    val schema = a.findValue("schema")?.toString()?.trim() ?: KomapperSequence.SCHEMA
                    val alwaysQuote =
                        a.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: KomapperSequence.ALWAYS_QUOTE
                    IdKind.Sequence(a, name, startWith, incrementBy, catalog, schema, alwaysQuote)
                }
            }
        }
        val idKinds = listOfNotNull(autoIncrement, sequence)
        if (idKinds.size > 1) {
            val iterator = idKinds.iterator()
            val a1 = iterator.next().annotation
            val a2 = iterator.next().annotation
            report("$a1 and $a2 cannot coexist on the same property.", parameter)
        }
        return idKinds.firstOrNull()
    }

    private fun createPropertyKind(parameter: KSValueParameter, idKind: IdKind?): PropertyKind? {
        var embedded: PropertyKind.Embedded? = null
        var embeddedId: PropertyKind.EmbeddedId? = null
        var id: PropertyKind.Id? = null
        var version: PropertyKind.Version? = null
        var createdAt: PropertyKind.CreatedAt? = null
        var updatedAt: PropertyKind.UpdatedAt? = null
        var ignore: PropertyKind.Ignore? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                KomapperEmbedded::class.simpleName -> embedded = PropertyKind.Embedded(a)
                KomapperEmbeddedId::class.simpleName -> embeddedId = PropertyKind.EmbeddedId(a)
                KomapperId::class.simpleName -> id = PropertyKind.Id(a, idKind)
                KomapperVersion::class.simpleName -> version = PropertyKind.Version(a)
                KomapperCreatedAt::class.simpleName -> createdAt = PropertyKind.CreatedAt(a)
                KomapperUpdatedAt::class.simpleName -> updatedAt = PropertyKind.UpdatedAt(a)
                KomapperIgnore::class.simpleName -> ignore = PropertyKind.Ignore(a)
            }
        }
        val kinds = listOfNotNull(embedded, embeddedId, id, version, createdAt, updatedAt, ignore)
        if (kinds.size > 1) {
            val iterator = kinds.iterator()
            val a1 = iterator.next().annotation
            val a2 = iterator.next().annotation
            report("$a1 and $a2 cannot coexist on the same property.", parameter)
        }
        return kinds.firstOrNull().also { kind ->
            if (idKind != null && kind !is PropertyKind.Id) {
                report(
                    "${idKind.annotation} and @${KomapperId::class.simpleName} must coexist on the same property.",
                    parameter
                )
            }
        }
    }

    private fun createEnumStrategy(parameter: KSValueParameter): EnumStrategy {
        val annotation = parameter.findAnnotation(KomapperEnum::class)
        return getEnumStrategy(annotation)
    }

    private fun getEnumStrategy(annotation: KSAnnotation?): EnumStrategy {
        return when (annotation?.findValue("type")?.toString()) {
            Symbols.EnumType_NAME -> EnumStrategy.NAME
            Symbols.EnumType_ORDINAL -> EnumStrategy.ORDINAL
            else -> config.enumStrategy
        }
    }
}
