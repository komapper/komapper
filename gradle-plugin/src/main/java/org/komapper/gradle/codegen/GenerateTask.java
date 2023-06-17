package org.komapper.gradle.codegen;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.komapper.codegen.CodeGenerator;
import org.komapper.codegen.MetadataReader;
import org.komapper.codegen.Table;

public class GenerateTask extends DefaultTask {

  private final Generator settings;

  @Inject
  public GenerateTask(Generator settings) {
    this.settings = Objects.requireNonNull(settings);
  }

  @TaskAction
  public void run() throws ClassNotFoundException, SQLException, IOException {
    List<Table> tables = read();
    generate(tables);
  }

  private List<Table> read() throws ClassNotFoundException, SQLException {
    var jdbc = settings.getJdbc();
    try (var connection = createConnection(jdbc)) {
      MetadataReader reader =
          new MetadataReader(
              settings.getEnquote().get(),
              connection.getMetaData(),
              settings.getCatalog().getOrNull(),
              settings.getSchemaPattern().getOrNull(),
              settings.getTableNamePattern().getOrNull(),
              settings.getTableTypes().get());
      return reader.read();
    }
  }

  private Connection createConnection(Jdbc jdbc) throws ClassNotFoundException, SQLException {
    String driverClassName = jdbc.getDriver().get();
    String url = jdbc.getUrl().get();
    String user = jdbc.getUser().get();
    String password = jdbc.getPassword().get();
    Class.forName(driverClassName);
    return DriverManager.getConnection(url, user, password);
  }

  private void generate(List<Table> tables) throws IOException {
    CodeGenerator generator =
        new CodeGenerator(
            settings.getPackageName().getOrNull(),
            tables,
            settings.getClassNameResolver().get(),
            settings.getPropertyNameResolver().get());
    var destinationDir = settings.getDestinationDir().get().getAsFile().toPath();
    try (var writer =
        generator.createNewFile(
            destinationDir, "entities.kt", settings.getOverwriteEntities().get())) {
      generator.generateEntities(
          writer,
          settings.getDeclareAsNullable().get(),
          settings.getUseSelfMapping().get(),
          settings.getUseCatalog().get(),
          settings.getUseSchema().get(),
          settings.getPropertyTypeResolver().get(),
          settings.getVersionPropertyName().get(),
          settings.getCreatedAtPropertyName().get(),
          settings.getUpdatedAtPropertyName().get());
    }
    if (!settings.getUseSelfMapping().get()) {
      try (var writer =
          generator.createNewFile(
              destinationDir, "entityDefinitions.kt", settings.getOverwriteDefinitions().get())) {
        generator.generateDefinitions(
            writer,
            settings.getUseCatalog().get(),
            settings.getUseSchema().get(),
            settings.getVersionPropertyName().get(),
            settings.getCreatedAtPropertyName().get(),
            settings.getUpdatedAtPropertyName().get());
      }
    }
  }
}
