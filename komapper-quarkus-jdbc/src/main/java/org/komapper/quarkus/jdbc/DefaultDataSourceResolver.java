package org.komapper.quarkus.jdbc;

import io.quarkus.agroal.runtime.AgroalDataSourceUtil;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;
import java.util.Objects;
import javax.sql.DataSource;

@Singleton
@DefaultBean
@Unremovable
public class DefaultDataSourceResolver implements DataSourceResolver {

  @Override
  public DataSource resolve(String dataSourceName) {
    Objects.requireNonNull(dataSourceName);
    return AgroalDataSourceUtil.dataSourceInstance(dataSourceName).get();
  }
}
