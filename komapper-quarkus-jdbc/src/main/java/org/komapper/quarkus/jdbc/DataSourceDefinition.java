package org.komapper.quarkus.jdbc;

public class DataSourceDefinition {
  public String name;
  public boolean isDefault;
  public String driver;
  public int batchSize;
  public int fetchSize;
  public int maxRows;
  public int queryTimeout;

  @Override
  public String toString() {
    return "DataSourceDefinition{"
        + "name='"
        + name
        + '\''
        + ", isDefault="
        + isDefault
        + ", dbKind="
        + driver
        + ", batchSize="
        + batchSize
        + ", fetchSize="
        + fetchSize
        + ", maxRows="
        + maxRows
        + ", queryTimeout="
        + queryTimeout
        + '}';
  }
}
