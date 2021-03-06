package org.komapper.codegen

import org.komapper.core.SnakeToLowerCamelCase
import org.komapper.core.SnakeToUpperCamelCase
import org.komapper.jdbc.dsl.query.MetadataQuery
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writer

class CodeGenerator(
    private val destinationDir: Path,
    private val packageName: String? = null,
    private val prefix: String = "",
    private val suffix: String = "",
    private val tables: List<MetadataQuery.Table>
) {

    fun generateEntities(
        fileName: String = "entities.kt",
        overwrite: Boolean = false,
        declareAsNullable: Boolean = false,
        resolver: ClassResolver
    ) {
        val file = createFilePath(fileName)
        if (Files.exists(file) && !overwrite) {
            return
        }
        mkdirs(file.parent)
        PrintWriter(file.writer()).use { p ->
            if (packageName != null) {
                p.println("package $packageName")
            }
            for (table in tables) {
                p.println()
                val className = SnakeToUpperCamelCase.apply(table.name)
                p.println("data class $prefix$className$suffix (")
                for (column in table.columns) {
                    val propertyName = SnakeToLowerCamelCase.apply(column.name)
                    val nullable = if (declareAsNullable || column.nullable) "?" else ""
                    val klass = resolver.resolve(column) ?: String::class
                    val propertyClassName = if (klass.qualifiedName?.removePrefix("kotlin.") == klass.simpleName) {
                        klass.simpleName
                    } else {
                        klass.qualifiedName
                    }
                    p.println("    val $propertyName: ${propertyClassName}$nullable,")
                }
                p.println(")")
            }
        }
    }

    fun generateDefinitions(
        fileName: String = "entityDefinitions.kt",
        overwrite: Boolean = false,
        useCatalog: Boolean = false,
        useSchema: Boolean = false,
    ) {
        val file = createFilePath(fileName)
        if (Files.exists(file) && !overwrite) {
            return
        }
        mkdirs(file.parent)
        PrintWriter(file.writer()).use { p ->
            if (packageName != null) {
                p.println("package $packageName")
                p.println()
            }
            p.println("import org.komapper.annotation.*")
            for (table in tables) {
                p.println()
                val className = SnakeToUpperCamelCase.apply(table.name)
                p.println("@KomapperEntityDef($className::class)")
                val kmTableArgs = listOfNotNull(
                    table.name,
                    if (useCatalog) table.catalog else null,
                    if (useSchema) table.schema else null
                ).joinToString { "\"$it\"" }
                p.println("@KomapperTable($kmTableArgs)")
                p.println("data class $prefix$className${suffix}Def (")
                for (column in table.columns) {
                    val propertyName = SnakeToLowerCamelCase.apply(column.name)
                    val id = if (column.isPrimaryKey) "@KomapperId " else ""
                    val autoIncrement = if (column.isAutoIncrement) "@KomapperAutoIncrement " else ""
                    p.println("    $id$autoIncrement@KomapperColumn(\"${column.name}\") val $propertyName: Nothing,")
                }
                p.println(") {")
                p.println("    companion object")
                p.println("}")
            }
        }
    }

    private fun createFilePath(name: String): Path {
        val packageDir = if (packageName == null) {
            destinationDir
        } else {
            val path = packageName.replace(".", "/")
            destinationDir.resolve(path)
        }
        return packageDir.resolve(name)
    }

    private fun mkdirs(dir: Path) {
        Files.createDirectories(dir)
    }
}
