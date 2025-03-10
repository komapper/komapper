package org.komapper.gradle.codegen;

import java.util.Collections;
import java.util.Objects;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.komapper.codegen.*;

public class Generator {
  private final String name;
  private final Jdbc jdbc;
  private final Property<String> catalog;
  private final Property<String> schemaPattern;
  private final Property<String> tableNamePattern;
  private final ListProperty<String> tableTypes;
  private final DirectoryProperty destinationDir;
  private final Property<String> packageName;
  private final Property<String> prefix;
  private final Property<String> suffix;
  private final Property<Boolean> singularize;
  private final Property<Boolean> overwriteEntities;
  private final Property<Boolean> declareAsNullable;
  private final Property<Boolean> useSelfMapping;
  private final Property<Boolean> useTableNameAsAlias;
  private final Property<Boolean> overwriteDefinitions;
  private final Property<Boolean> useCatalog;
  private final Property<Boolean> useSchema;
  private final Property<PropertyTypeResolver> propertyTypeResolver;
  private final Property<Enquote> enquote;

  private final Property<PackageNameResolver> packageNameResolver;
  private final Property<ClassNameResolver> classNameResolver;
  private final Property<PropertyNameResolver> propertyNameResolver;
  private final Property<String> versionPropertyName;
  private final Property<String> createdAtPropertyName;
  private final Property<String> updatedAtPropertyName;

  @Inject
  public Generator(String name, Project project) {
    this.name = Objects.requireNonNull(name);
    Objects.requireNonNull(project);
    ObjectFactory objects = project.getObjects();

    this.jdbc = objects.newInstance(Jdbc.class);
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
    this.singularize = objects.property(Boolean.class).value(false);
    this.overwriteEntities = objects.property(Boolean.class).value(false);
    this.declareAsNullable = objects.property(Boolean.class).value(false);
    this.useSelfMapping = objects.property(Boolean.class).value(false);
    this.useTableNameAsAlias = objects.property(Boolean.class).value(false);
    this.overwriteDefinitions = objects.property(Boolean.class).value(false);
    this.useCatalog = objects.property(Boolean.class).value(false);
    this.useSchema = objects.property(Boolean.class).value(false);
    this.propertyTypeResolver =
        objects.property(PropertyTypeResolver.class).value(PropertyTypeResolver.of());
    this.enquote = objects.property(Enquote.class);
    enquote.set(jdbc.getUrl().map(Enquote::of));
    this.packageNameResolver =
        objects.property(PackageNameResolver.class).value(PackageNameResolver.of());
    this.classNameResolver = objects.property(ClassNameResolver.class);
    classNameResolver.set(
        prefix.zip(suffix, Pair::of).zip(singularize, (a, b) -> ClassNameResolver.of(a.a, a.b, b)));
    this.propertyNameResolver =
        objects.property(PropertyNameResolver.class).value(PropertyNameResolver.of());
    this.versionPropertyName = objects.property(String.class).value("");
    this.createdAtPropertyName = objects.property(String.class).value("");
    this.updatedAtPropertyName = objects.property(String.class).value("");
  }

  public String getName() {
    return name;
  }

  public Jdbc getJdbc() {
    return jdbc;
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

  public Property<Boolean> getSingularize() {
    return singularize;
  }

  public Property<Boolean> getOverwriteEntities() {
    return overwriteEntities;
  }

  public Property<Boolean> getDeclareAsNullable() {
    return declareAsNullable;
  }

  public Property<Boolean> getUseSelfMapping() {
    return useSelfMapping;
  }

  public Property<Boolean> getUseTableNameAsAlias() {
    return useTableNameAsAlias;
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

  public Property<PropertyTypeResolver> getPropertyTypeResolver() {
    return propertyTypeResolver;
  }

  public Property<Enquote> getEnquote() {
    return enquote;
  }

  public Property<PackageNameResolver> getPackageNameResolver() {
    return packageNameResolver;
  }

  public Property<ClassNameResolver> getClassNameResolver() {
    return classNameResolver;
  }

  public Property<PropertyNameResolver> getPropertyNameResolver() {
    return propertyNameResolver;
  }

  public Property<String> getVersionPropertyName() {
    return versionPropertyName;
  }

  public Property<String> getCreatedAtPropertyName() {
    return createdAtPropertyName;
  }

  public Property<String> getUpdatedAtPropertyName() {
    return updatedAtPropertyName;
  }

  public void jdbc(Action<Jdbc> action) {
    action.execute(jdbc);
  }
}
