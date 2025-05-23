package org.komapper.quarkus.jdbc.deployment;

import static java.util.stream.Collectors.toList;

import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jboss.logging.Logger;
import org.komapper.quarkus.jdbc.DataSourceDefinition;
import org.komapper.quarkus.jdbc.KomapperSettings;

public class KomapperSettingsFactory {

  private static final Logger logger = Logger.getLogger(KomapperSettingsFactory.class);

  private final KomapperBuildTimeConfig buildTimeConfig;
  private final List<JdbcDataSourceBuildItem> dataSources;

  KomapperSettingsFactory(
      KomapperBuildTimeConfig buildTimeConfig, List<JdbcDataSourceBuildItem> dataSources) {
    this.buildTimeConfig = Objects.requireNonNull(buildTimeConfig);
    this.dataSources = new ArrayList<>(Objects.requireNonNull(dataSources));
  }

  KomapperSettings create() {
    var dataSourceDefinitions = createDataSourceDefinitions();
    if (dataSourceDefinitions.isEmpty()) {
      throw new IllegalStateException("The quarkus.datasource is empty. Specify it.");
    }
    var komapperSettings = new KomapperSettings(dataSourceDefinitions);
    logger.debugf("komapperSettings: %s", komapperSettings);
    return komapperSettings;
  }

  private List<DataSourceDefinition> createDataSourceDefinitions() {
    return dataSources.stream()
        .map(
            dataSource -> {
              var dataSourceConfig = buildTimeConfig.dataSources().get(dataSource.getName());
              return createDataSourceDefinition(dataSource, dataSourceConfig);
            })
        .collect(toList());
  }

  private DataSourceDefinition createDataSourceDefinition(
      JdbcDataSourceBuildItem dataSource,
      KomapperBuildTimeConfig.DataSourceBuildTimeConfig dataSourceConfig) {
    var definition = new DataSourceDefinition();
    definition.name = dataSource.getName();
    definition.isDefault = dataSource.isDefault();
    definition.driver = convertToDriverName(dataSource.getDbKind());
    definition.batchSize = dataSourceConfig.batchSize();
    definition.fetchSize = dataSourceConfig.fetchSize();
    definition.maxRows = dataSourceConfig.maxRows();
    definition.queryTimeout = dataSourceConfig.queryTimeout();
    return definition;
  }

  private String convertToDriverName(String dbKind) {
    switch (dbKind) {
      case "h2":
      case "mysql":
      case "mariadb":
      case "oracle":
        return dbKind;
      case "postgresql":
      case "pgsql":
      case "pg":
        return "postgresql";
      case "sqlserver":
      case "mssql":
        return "sqlserver";
      default:
        throw new IllegalStateException(
            "Can't infer the driver name from the dbKind \""
                + dbKind
                + "\". The dbKind is illegal or not supported.");
    }
  }
}
