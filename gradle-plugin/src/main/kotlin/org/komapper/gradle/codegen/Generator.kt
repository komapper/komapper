package org.komapper.gradle.codegen

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.komapper.core.Database
import javax.inject.Inject

open class Generator @Inject constructor(val name: String, project: Project) {
    private val objects = project.objects
    val database: Property<Database> = objects.property(Database::class.java)
    val catalog: Property<String> = objects.property(String::class.java)
    val schemaPattern: Property<String> = objects.property(String::class.java)
    val tableNamePattern: Property<String> = objects.property(String::class.java)
    val tableTypes: ListProperty<String> = objects.listProperty(String::class.java).value(listOf("TABLE"))
    val destinationDir: DirectoryProperty = objects.directoryProperty().apply {
        set(project.layout.projectDirectory.dir("src/main/kotlin"))
    }
    val packageName: Property<String> = objects.property(String::class.java)
    val prefix: Property<String> = objects.property(String::class.java).value("")
    val suffix: Property<String> = objects.property(String::class.java).value("")
    val overwriteEntities: Property<Boolean> = objects.property(Boolean::class.java).value(false)
    val declareAsNullable: Property<Boolean> = objects.property(Boolean::class.java).value(false)
    val overwriteDefinitions: Property<Boolean> = objects.property(Boolean::class.java).value(false)
    val useCatalog: Property<Boolean> = objects.property(Boolean::class.java).value(false)
    val useSchema: Property<Boolean> = objects.property(Boolean::class.java).value(false)
}
