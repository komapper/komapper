package org.komapper.gradle.codegen

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.komapper.codegen.ClassResolver
import org.komapper.codegen.CodeGenerator
import org.komapper.core.Database
import org.komapper.core.dsl.MetadataDsl
import org.komapper.core.dsl.runQuery
import org.komapper.core.jdbc.Table
import javax.inject.Inject

open class GenerateTask @Inject internal constructor(private val settings: Generator) : DefaultTask() {

    @TaskAction
    fun run() {
        val database = settings.database.get()
        val tables = read(database)
        generate(database, tables)
    }

    private fun read(database: Database): List<Table> {
        return database.runQuery {
            MetadataDsl.tables(
                schemaPattern = settings.schemaPattern.orNull,
                tableNamePattern = settings.tableNamePattern.orNull,
                tableTypes = settings.tableTypes.get()
            )
        }
    }

    private fun generate(database: Database, tables: List<Table>) {
        val generator = CodeGenerator(
            destinationDir = settings.destinationDir.get().asFile.toPath(),
            packageName = settings.packageName.orNull,
            prefix = settings.prefix.get(),
            suffix = settings.suffix.get(),
            tables = tables
        )
        generator.generateEntities(
            resolver = ClassResolver.create(database),
            overwrite = settings.overwriteEntities.get()
        )
        generator.generateDefinitions(
            overwrite = settings.overwriteDefinitions.get(),
            useCatalog = settings.useCatalog.get(),
            useSchema = settings.useSchema.get()
        )
    }
}
