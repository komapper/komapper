package org.komapper.codegen;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CodeGenerator {

  private final String packageName;
  private final List<Table> tables;
  private final PackageNameResolver packageNameResolver;
  private final ClassNameResolver classNameResolver;
  private final PropertyNameResolver propertyNameResolver;

  public CodeGenerator(
      @Nullable String packageName,
      @NotNull List<Table> tables,
      @NotNull PackageNameResolver packageNameResolver,
      @NotNull ClassNameResolver classNameResolver,
      @NotNull PropertyNameResolver propertyNameResolver) {
    this.packageName = packageName;
    this.tables = new ArrayList<>(Objects.requireNonNull(tables));
    this.classNameResolver = Objects.requireNonNull(classNameResolver);
    this.packageNameResolver = Objects.requireNonNull(packageNameResolver);
    this.propertyNameResolver = Objects.requireNonNull(propertyNameResolver);
  }

  @NotNull
  public Writer createNewFile(
      @NotNull Path destinationDir, @NotNull String fileName, boolean overwrite)
      throws IOException {
    Objects.requireNonNull(destinationDir);
    var file = createFilePath(destinationDir, fileName);
    if (Files.exists(file) && !overwrite) {
      return Writer.nullWriter();
    }
    Files.createDirectories(file.getParent());
    return new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8);
  }

  public void generateEntities(
      @NotNull Writer writer,
      boolean declareAsNullable,
      boolean useSelfMapping,
      boolean useTableNameAsAlias,
      boolean useCatalog,
      boolean useSchema,
      @NotNull PropertyTypeResolver resolver,
      @NotNull String versionPropertyName,
      @NotNull String createdAtPropertyName,
      @NotNull String updatedAtPropertyName) {
    Objects.requireNonNull(writer);
    Objects.requireNonNull(resolver);
    var p = new PrintWriter(writer);
    if (packageName != null) {
      p.println("package " + packageNameResolver.resolve(packageName));
    }
    if (useSelfMapping) {
      p.println();
      p.print(
          createImports(true, versionPropertyName, createdAtPropertyName, updatedAtPropertyName));
    }
    for (Table table : tables) {
      p.println();
      var className = classNameResolver.resolve(table);
      if (useSelfMapping) {
        p.print("@KomapperEntity");
        if (useTableNameAsAlias) {
          p.print("([\"" + StringUtil.snakeToLowerCamelCase(table.getName()) + "\"])");
        }
        p.println();
        p.println(createTableAnnotation(table, useCatalog, useSchema));
      }
      p.println("data class " + className + " (");
      for (Column column : table.getColumns()) {
        var propertyName = propertyNameResolver.resolve(column);
        var nullable = (declareAsNullable || column.isNullable()) ? "?" : "";
        var propertyClassName = resolver.resolve(table, column);
        var propertyType = propertyClassName + nullable;
        if (useSelfMapping) {
          p.println(
              createPropertyDefinition(
                  column,
                  propertyName,
                  propertyType,
                  versionPropertyName,
                  createdAtPropertyName,
                  updatedAtPropertyName));
        } else {
          p.println("    val " + propertyName + ": " + propertyType + ",");
        }
      }
      p.println(")");
    }
  }

  public void generateDefinitions(
      @NotNull Writer writer,
      boolean useTableNameAsAlias,
      boolean useCatalog,
      boolean useSchema,
      @NotNull String versionPropertyName,
      @NotNull String createdAtPropertyName,
      @NotNull String updatedAtPropertyName) {
    Objects.requireNonNull(writer);
    var p = new PrintWriter(writer);
    if (packageName != null) {
      p.println("package " + packageNameResolver.resolve(packageName));
      p.println();
    }
    p.print(
        createImports(false, versionPropertyName, createdAtPropertyName, updatedAtPropertyName));
    for (Table table : tables) {
      p.println();
      var className = classNameResolver.resolve(table);
      p.print("@KomapperEntityDef(" + className + "::class");
      if (useTableNameAsAlias) {
        p.print(", [\"" + StringUtil.snakeToLowerCamelCase(table.getName()) + "\"]");
      }
      p.println(")");
      p.println(createTableAnnotation(table, useCatalog, useSchema));
      p.println("data class " + className + "Def (");
      for (Column column : table.getColumns()) {
        var propertyName = propertyNameResolver.resolve(column);
        p.println(
            createPropertyDefinition(
                column,
                propertyName,
                "Nothing",
                versionPropertyName,
                createdAtPropertyName,
                updatedAtPropertyName));
      }
      p.println(")");
    }
  }

  private String createImports(
      boolean isEntity,
      @NotNull String versionPropertyName,
      @NotNull String createdAtPropertyName,
      @NotNull String updatedAtPropertyName) {
    var sb = new StringBuilder();
    sb.append("import org.komapper.annotation.KomapperAutoIncrement")
        .append(System.lineSeparator());
    sb.append("import org.komapper.annotation.KomapperColumn").append(System.lineSeparator());
    if (!createdAtPropertyName.isBlank()) {
      sb.append("import org.komapper.annotation.KomapperCreatedAt").append(System.lineSeparator());
    }
    if (isEntity) {
      sb.append("import org.komapper.annotation.KomapperEntity").append(System.lineSeparator());
    } else {
      sb.append("import org.komapper.annotation.KomapperEntityDef").append(System.lineSeparator());
    }
    sb.append("import org.komapper.annotation.KomapperId").append(System.lineSeparator());
    sb.append("import org.komapper.annotation.KomapperTable").append(System.lineSeparator());
    if (!updatedAtPropertyName.isBlank()) {
      sb.append("import org.komapper.annotation.KomapperUpdatedAt").append(System.lineSeparator());
    }
    if (!versionPropertyName.isBlank()) {
      sb.append("import org.komapper.annotation.KomapperVersion").append(System.lineSeparator());
    }
    return sb.toString();
  }

  private String createTableAnnotation(
      @NotNull Table table, boolean useCatalog, boolean useSchema) {
    var tableArgs = new StringBuilder();
    tableArgs.append('"').append(table.getName()).append('"');
    if (useCatalog && table.getCatalog() != null) {
      tableArgs.append(", ");
      tableArgs.append('"').append(table.getCatalog()).append('"');
    }
    if (useSchema && table.getSchema() != null) {
      tableArgs.append(", ");
      tableArgs.append('"').append(table.getSchema()).append('"');
    }
    return "@KomapperTable(" + tableArgs + ")";
  }

  private String createPropertyDefinition(
      @NotNull Column column,
      @NotNull String propertyName,
      @NotNull String propertyType,
      @NotNull String versionPropertyName,
      @NotNull String createdAtPropertyName,
      @NotNull String updatedAtPropertyName) {
    var id = column.isPrimaryKey() ? "@KomapperId " : "";
    var autoIncrement = column.isAutoIncrement() ? "@KomapperAutoIncrement " : "";
    var version = propertyName.equals(versionPropertyName) ? "@KomapperVersion " : "";
    var createdAt = propertyName.equals(createdAtPropertyName) ? "@KomapperCreatedAt " : "";
    var updatedAt = propertyName.equals(updatedAtPropertyName) ? "@KomapperUpdatedAt " : "";
    return "    "
        + id
        + autoIncrement
        + version
        + createdAt
        + updatedAt
        + "@KomapperColumn("
        + '"'
        + column.getName()
        + '"'
        + ") val "
        + propertyName
        + ": "
        + propertyType
        + ",";
  }

  private Path createFilePath(Path destinationDir, String name) {
    Path packageDir;
    if (packageName == null) {
      packageDir = destinationDir;
    } else {
      String path = packageName.replace(".", "/");
      packageDir = destinationDir.resolve(path);
    }
    return packageDir.resolve(name);
  }
}
