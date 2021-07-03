package org.komapper.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperIgnore
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.core.NamingStrategy

internal class EntityDefFactory(
    config: Config,
    private val definitionSource: EntityDefinitionSource
) {
    private val namingStrategy: NamingStrategy = config.namingStrategy
    private val defDeclaration = definitionSource.defDeclaration

    fun create(): EntityDef {
        val table = getTable()
        val allProperties = createAllProperties()
        val companionObject = getCompanionObject()
        return EntityDef(definitionSource, table, allProperties, companionObject)
    }

    private fun getTable(): Table {
        val annotation = defDeclaration.findAnnotation(KomapperTable::class)
        val name = annotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(definitionSource.entityDeclaration.simpleName.asString())
        val catalog = annotation?.findValue("catalog")?.toString()?.trim() ?: KomapperTable.catalog
        val schema = annotation?.findValue("schema")?.toString()?.trim() ?: KomapperTable.schema
        val alwaysQuote =
            annotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: KomapperTable.alwaysQuote
        return Table(name, catalog, schema, alwaysQuote)
    }

    private fun getColumn(parameter: KSValueParameter): Column {
        val annotation = parameter.findAnnotation(KomapperColumn::class)
        val name = annotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(parameter.toString())
        val alwaysQuote =
            annotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: KomapperColumn.alwaysQuote
        return Column(name, alwaysQuote)
    }

    private fun createAllProperties(): List<PropertyDef> {
        val propertyDeclarationMap = defDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        return defDeclaration.primaryConstructor?.parameters
            ?.asSequence()
            ?.map { parameter ->
                val declaration = propertyDeclarationMap[parameter.name]
                    ?: report("The corresponding property declaration is not found.", parameter)
                val column = getColumn(parameter)
                val idKind = createIdKind(parameter)
                val kind = createPropertyKind(parameter, idKind)
                PropertyDef(parameter, declaration, column, kind)
            }?.also {
                validateProperties(it)
            }?.toList() ?: emptyList()
    }

    private fun validateProperties(properties: Sequence<PropertyDef>) {
        if (properties.anyDuplicates { it.kind is PropertyKind.Version }) {
            report("Multiple @${KomapperVersion::class.simpleName} cannot coexist in a single class.", defDeclaration)
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.CreatedAt }) {
            report("Multiple @${KomapperCreatedAt::class.simpleName} cannot coexist in a single class.", defDeclaration)
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.UpdatedAt }) {
            report("Multiple @${KomapperUpdatedAt::class.simpleName} cannot coexist in a single class.", defDeclaration)
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
                    val startWith = a.findValue("startWith") ?: KomapperSequence.startWith
                    val incrementBy = a.findValue("incrementBy") ?: KomapperSequence.incrementBy
                    val catalog = a.findValue("catalog")?.toString()?.trim() ?: KomapperSequence.catalog
                    val schema = a.findValue("schema")?.toString()?.trim() ?: KomapperSequence.schema
                    val alwaysQuote =
                        a.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: KomapperSequence.alwaysQuote
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
        var id: PropertyKind.Id? = null
        var version: PropertyKind.Version? = null
        var createdAt: PropertyKind.CreatedAt? = null
        var updatedAt: PropertyKind.UpdatedAt? = null
        var ignore: PropertyKind.Ignore? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                KomapperId::class.simpleName -> id = PropertyKind.Id(a, idKind)
                KomapperVersion::class.simpleName -> version = PropertyKind.Version(a)
                KomapperCreatedAt::class.simpleName -> createdAt = PropertyKind.CreatedAt(a)
                KomapperUpdatedAt::class.simpleName -> updatedAt = PropertyKind.UpdatedAt(a)
                KomapperIgnore::class.simpleName -> ignore = PropertyKind.Ignore(a)
            }
        }
        val kinds = listOfNotNull(id, version, createdAt, updatedAt, ignore)
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

    private fun getCompanionObject(): KSClassDeclaration {
        return defDeclaration.getCompanionObject()
            ?: report("Define a companion object in the class.", defDeclaration)
    }
}
