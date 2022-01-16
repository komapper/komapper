package org.komapper.quarkus.jdbc;

import javax.sql.DataSource;

public interface DataSourceResolver {
  DataSource resolve(String dataSourceName);
}
