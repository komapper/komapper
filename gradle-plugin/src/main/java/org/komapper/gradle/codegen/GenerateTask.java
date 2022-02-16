package org.komapper.gradle.codegen;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.komapper.codegen.CodeGenerator;
import org.komapper.codegen.MetadataReader;
import org.komapper.codegen.Table;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class GenerateTask extends DefaultTask {

  private static final Pattern JDBC_URL_PATTERN = Pattern.compile("^jdbc:(tc:)?([^:]*):.*");
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
      var url = jdbc.getUrl().get();
      var driver = extractDriver(url);
      // TODO: Remove this workaround in the future
      // https://jira.mariadb.org/browse/CONJ-921
      var catalog = settings.getCatalog().getOrNull();
      if (driver.equals("mariadb")) {
        catalog = "";
      }
      MetadataReader reader =
          new MetadataReader(
              settings.getEnquote().get(),
              connection.getMetaData(),
              catalog,
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

  private String extractDriver(String url) {
    var matcher = JDBC_URL_PATTERN.matcher(url);
    if (matcher.matches()) {
      return matcher.group(2).toLowerCase();
    }
    throw new IllegalArgumentException("The driver in the JDBC URL is not found. url=$url");
  }

  private void generate(List<Table> tables) throws IOException {
    CodeGenerator generator =
        new CodeGenerator(
            settings.getPackageName().getOrNull(),
            settings.getPrefix().get(),
            settings.getSuffix().get(),
            tables);
    var destinationDir = settings.getDestinationDir().get().getAsFile().toPath();
    try (var writer =
        generator.createNewFile(
            destinationDir, "entities.kt", settings.getOverwriteEntities().get())) {
      generator.generateEntities(
          writer, settings.getDeclareAsNullable().get(), settings.getPropertyTypeResolver().get());
    }
    try (var writer =
        generator.createNewFile(
            destinationDir, "entityDefinitions.kt", settings.getOverwriteDefinitions().get())) {
      generator.generateDefinitions(
          writer, settings.getUseCatalog().get(), settings.getUseSchema().get());
    }
  }
}
