package org.komapper.gradle.codegen

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.komapper.codegen.CodeGenerator
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.dsl.MetadataDsl.tables
import org.komapper.jdbc.dsl.query.MetadataQuery
import javax.inject.Inject

open class GenerateTask @Inject internal constructor(private val settings: Generator) : DefaultTask() {

    @TaskAction
    fun run() {
        val database = settings.database.get()
        val tables = read(database)
        generate(tables)
    }

    private fun read(database: JdbcDatabase): List<MetadataQuery.Table> {
        val metadataQuery = tables(
            catalog = settings.catalog.orNull,
            schemaPattern = settings.schemaPattern.orNull,
            tableNamePattern = settings.tableNamePattern.orNull,
            tableTypes = settings.tableTypes.get()
        )
        return metadataQuery.run(database.config)
    }

    private fun generate(tables: List<MetadataQuery.Table>) {
        val generator = CodeGenerator(
            destinationDir = settings.destinationDir.get().asFile.toPath(),
            packageName = settings.packageName.orNull,
            prefix = settings.prefix.get(),
            suffix = settings.suffix.get(),
            tables = tables
        )
        generator.generateEntities(
            resolver = settings.classResolver.get(),
            overwrite = settings.overwriteEntities.get(),
            declareAsNullable = settings.declareAsNullable.get()
        )
        generator.generateDefinitions(
            overwrite = settings.overwriteDefinitions.get(),
            useCatalog = settings.useCatalog.get(),
            useSchema = settings.useSchema.get()
        )
    }
}
