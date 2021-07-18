package org.komapper.gradle.codegen;

import java.util.Collections;
import java.util.Objects;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.komapper.codegen.ClassResolver;
import org.komapper.jdbc.JdbcDatabase;

public class Generator {
  private final String name;
  private final Property<JdbcDatabase> database;
  private final Property<String> catalog;
  private final Property<String> schemaPattern;
  private final Property<String> tableNamePattern;
  private final ListProperty<String> tableTypes;
  private final DirectoryProperty destinationDir;
  private final Property<String> packageName;
  private final Property<String> prefix;
  private final Property<String> suffix;
  private final Property<Boolean> overwriteEntities;
  private final Property<Boolean> declareAsNullable;
  private final Property<Boolean> overwriteDefinitions;
  private final Property<Boolean> useCatalog;
  private final Property<Boolean> useSchema;
  private final Property<ClassResolver> classResolver;

  @Inject
  public Generator(String name, Project project) {
    this.name = Objects.requireNonNull(name);
    Objects.requireNonNull(project);
    ObjectFactory objects = project.getObjects();

    this.database = objects.property(JdbcDatabase.class);
    this.catalog = objects.property(String.class);
    this.schemaPattern = objects.property(String.class);
    this.tableNamePattern = objects.property(String.class);
    this.tableTypes = objects.listProperty(String.class).value(Collections.singletonList("TABLE"));
    this.destinationDir =
        objects
            .directoryProperty()
            .value(project.getLayout().getProjectDirectory().dir("src/main/kotlin"));
    this.packageName = objects.property(String.class);
    this.prefix = objects.property(String.class).value("");
    this.suffix = objects.property(String.class).value("");
    this.overwriteEntities = objects.property(Boolean.class).value(false);
    this.declareAsNullable = objects.property(Boolean.class).value(false);
    this.overwriteDefinitions = objects.property(Boolean.class).value(false);
    this.useCatalog = objects.property(Boolean.class).value(false);
    this.useSchema = objects.property(Boolean.class).value(false);
    this.classResolver =
        objects
            .property(ClassResolver.class)
            .value(
                database.map(
                    it ->
                        ClassResolver.Companion.create(
                            it.getConfig().getDialect().getDataTypes())));
  }

  public String getName() {
    return name;
  }

  public Property<JdbcDatabase> getDatabase() {
    return database;
  }

  public Property<String> getCatalog() {
    return catalog;
  }

  public Property<String> getSchemaPattern() {
    return schemaPattern;
  }

  public Property<String> getTableNamePattern() {
    return tableNamePattern;
  }

  public ListProperty<String> getTableTypes() {
    return tableTypes;
  }

  public DirectoryProperty getDestinationDir() {
    return destinationDir;
  }

  public Property<String> getPackageName() {
    return packageName;
  }

  public Property<String> getPrefix() {
    return prefix;
  }

  public Property<String> getSuffix() {
    return suffix;
  }

  public Property<Boolean> getOverwriteEntities() {
    return overwriteEntities;
  }

  public Property<Boolean> getDeclareAsNullable() {
    return declareAsNullable;
  }

  public Property<Boolean> getOverwriteDefinitions() {
    return overwriteDefinitions;
  }

  public Property<Boolean> getUseCatalog() {
    return useCatalog;
  }

  public Property<Boolean> getUseSchema() {
    return useSchema;
  }

  public Property<ClassResolver> getClassResolver() {
    return classResolver;
  }
}
