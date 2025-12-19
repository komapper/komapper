package org.komapper.processor.entity

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperIgnore
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.processor.Context
import org.komapper.processor.findValue
import org.komapper.processor.hasDuplicates
import org.komapper.processor.report

internal class EntityDefFactory(
    private val context: Context,
    private val definitionSource: EntityDefinitionSource,
) {
    private val annotationSupport: AnnotationSupport = AnnotationSupport(context)
    private val defDeclaration = definitionSource.defDeclaration

    fun create(): EntityDef {
        val table = annotationSupport.getTable(definitionSource)
        val projection = annotationSupport.getProjection(definitionSource)
        val aggregateRoot = annotationSupport.getAggregateRoot(definitionSource)
        val associations = annotationSupport.getAssociations(definitionSource)
        validateAssociations(associations)
        val allProperties = createAllProperties()
        val compositeProperties = allProperties.filterIsInstance<CompositePropertyDef>()
        val leafProperties = allProperties.filterIsInstance<LeafPropertyDef>()
        validateCompositeProperties(compositeProperties)
        validateLeafProperties(leafProperties)
        return EntityDef(definitionSource, table, projection, aggregateRoot, associations, allProperties)
    }

    private fun createAllProperties(): List<PropertyDef> {
        val propertyDeclarationMap = defDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        val parameters = defDeclaration.primaryConstructor?.parameters ?: return emptyList()
        return parameters.map { parameter ->
            val declaration = propertyDeclarationMap[parameter.name]
                ?: report("The corresponding property declaration is not found.", parameter)
            when (val kind = createPropertyKind(parameter, null)) {
                is PropertyKind.Embedded, is PropertyKind.EmbeddedId -> {
                    CompositePropertyDef(parameter, declaration, kind)
                }

                else -> {
                    createLeafProperty(parameter, declaration)
                }
            }
        }
    }

    private fun createLeafProperty(parameter: KSValueParameter, declaration: KSPropertyDeclaration): LeafPropertyDef {
        val idKind = createIdKind(parameter)
        val kind = createPropertyKind(parameter, idKind)
        val column = annotationSupport.getColumn(parameter)
        val enumStrategy = annotationSupport.getEnumStrategy(parameter)
        return LeafPropertyDef(parameter, declaration, kind, column, enumStrategy)
    }

    private fun validateAssociations(association: List<Association>) {
        for (entry in association.groupingBy { it.navigator }.eachCount()) {
            if (entry.value > 1) {
                report("The navigator \"${entry.key}\" is found multiple times in the association annotations.", defDeclaration)
            }
        }
    }

    private fun validateCompositeProperties(properties: List<CompositePropertyDef>) {
        if (properties.hasDuplicates { it.kind is PropertyKind.EmbeddedId }) {
            report(
                "Multiple @${KomapperEmbeddedId::class.simpleName} cannot coexist in a single class.",
                defDeclaration,
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
                defDeclaration,
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
                        a.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: context.config.alwaysQuote
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
                KomapperEmbedded::class.simpleName -> {
                    embedded = PropertyKind.Embedded(a)
                }

                KomapperEmbeddedId::class.simpleName -> {
                    val virtual = a.findValue("virtual")
                    embeddedId = PropertyKind.EmbeddedId(a, virtual == true)
                }

                KomapperId::class.simpleName -> {
                    val virtual = a.findValue("virtual")
                    id = PropertyKind.Id(a, idKind, virtual == true)
                }

                KomapperVersion::class.simpleName -> {
                    version = PropertyKind.Version(a)
                }

                KomapperCreatedAt::class.simpleName -> {
                    createdAt = PropertyKind.CreatedAt(a)
                }

                KomapperUpdatedAt::class.simpleName -> {
                    updatedAt = PropertyKind.UpdatedAt(a)
                }

                KomapperIgnore::class.simpleName -> {
                    ignore = PropertyKind.Ignore(a)
                }
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
                    parameter,
                )
            }
        }
    }
}
