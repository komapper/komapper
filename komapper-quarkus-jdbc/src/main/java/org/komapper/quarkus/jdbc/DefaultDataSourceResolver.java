package org.komapper.quarkus.jdbc;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import javax.sql.DataSource;

@Singleton
@DefaultBean
@Unremovable
public class DefaultDataSourceResolver implements DataSourceResolver {

  private final DataSources dataSources;

  @Inject
  public DefaultDataSourceResolver(DataSources dataSources) {
    this.dataSources = Objects.requireNonNull(dataSources);
  }

  @Override
  public DataSource resolve(String dataSourceName) {
    Objects.requireNonNull(dataSourceName);
    return dataSources.getDataSource(dataSourceName);
  }
}
