package org.komapper.gradle

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.komapper.core.Database
import javax.inject.Inject

open class CodeGenExtension @Inject constructor(project: Project) {
    private val objects = project.objects
    val database: Property<Database> = objects.property(Database::class.java)
    val schemaPattern: Property<String> = objects.property(String::class.java)
    val tableNamePattern: Property<String> = objects.property(String::class.java)
    val tableTypes: ListProperty<String> = objects.listProperty(String::class.java)
    val destinationDir: DirectoryProperty = objects.directoryProperty()
    val packageName: Property<String> = objects.property(String::class.java)
    val prefix: Property<String> = objects.property(String::class.java)
    val suffix: Property<String> = objects.property(String::class.java)
    val overwriteEntities: Property<Boolean> = objects.property(Boolean::class.java)
    val overwriteDefinitions: Property<Boolean> = objects.property(Boolean::class.java)
    val useCatalog: Property<Boolean> = objects.property(Boolean::class.java)
    val useSchema: Property<Boolean> = objects.property(Boolean::class.java)

    init {
        tableTypes.set(listOf("TABLE"))
        destinationDir.set(project.layout.projectDirectory.dir("src/main/kotlin"))
        prefix.set("")
        suffix.set("")
        overwriteEntities.set(false)
        overwriteDefinitions.set(false)
        useCatalog.set(false)
        useSchema.set(false)
    }
}
