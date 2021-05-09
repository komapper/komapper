package org.komapper.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.komapper.codegen.ClassResolver
import org.komapper.codegen.CodeGenerator
import org.komapper.core.Database
import org.komapper.core.dsl.MetadataDsl
import org.komapper.core.dsl.runQuery
import org.komapper.core.jdbc.Table
import javax.inject.Inject

open class CodeGenerateTask @Inject internal constructor(private val extension: CodeGenExtension) : DefaultTask() {

    @TaskAction
    fun run() {
        val database = extension.database.get()
        val tables = read(database)
        generate(database, tables)
    }

    private fun read(database: Database): List<Table> {
        return database.runQuery {
            MetadataDsl.tables(
                schemaPattern = extension.schemaPattern.orNull,
                tableNamePattern = extension.tableNamePattern.orNull,
                tableTypes = extension.tableTypes.get()
            )
        }
    }

    private fun generate(database: Database, tables: List<Table>) {
        val generator = CodeGenerator(
            destinationDir = extension.destinationDir.get().asFile.toPath(),
            packageName = extension.packageName.orNull,
            prefix = extension.prefix.get(),
            suffix = extension.suffix.get(),
            tables = tables
        )
        generator.generateEntities(
            resolver = ClassResolver.create(database),
            overwrite = extension.overwriteEntities.get()
        )
        generator.generateDefinitions(
            overwrite = extension.overwriteDefinitions.get(),
            useCatalog = extension.useCatalog.get(),
            useSchema = extension.useSchema.get()
        )
    }
}
