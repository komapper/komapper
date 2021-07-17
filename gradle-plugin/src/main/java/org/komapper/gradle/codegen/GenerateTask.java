package org.komapper.gradle.codegen;

import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.komapper.codegen.CodeGenerator;
import org.komapper.jdbc.JdbcDatabase;
import org.komapper.jdbc.dsl.MetadataDsl;
import org.komapper.jdbc.dsl.query.MetadataQuery;

public class GenerateTask extends DefaultTask {
  private final Generator settings;

  @Inject
  public GenerateTask(Generator settings) {
    this.settings = Objects.requireNonNull(settings);
  }

  @TaskAction
  public void run() {
    JdbcDatabase database = settings.getDatabase().get();
    List<MetadataQuery.Table> tables = read(database);
    generate(tables);
  }

  private List<MetadataQuery.Table> read(JdbcDatabase database) {
    MetadataQuery metadataQuery =
        MetadataDsl.INSTANCE.tables(
            settings.getCatalog().getOrNull(),
            settings.getSchemaPattern().getOrNull(),
            settings.getTableNamePattern().getOrNull(),
            settings.getTableTypes().get());
    return metadataQuery.run(database.getConfig());
  }

  private void generate(List<MetadataQuery.Table> tables) {
    CodeGenerator generator =
        new CodeGenerator(
            settings.getDestinationDir().get().getAsFile().toPath(),
            settings.getPackageName().getOrNull(),
            settings.getPrefix().get(),
            settings.getSuffix().get(),
            tables);
    generator.generateEntities(
        "entities.kt",
        settings.getOverwriteEntities().get(),
        settings.getDeclareAsNullable().get(),
        settings.getClassResolver().get());
    generator.generateDefinitions(
        "entityDefinitions.kt",
        settings.getOverwriteDefinitions().get(),
        settings.getUseCatalog().get(),
        settings.getUseSchema().get());
  }
}
