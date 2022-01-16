package org.komapper.quarkus.jdbc.deployment;

import io.quarkus.runtime.annotations.*;
import java.util.Map;

@ConfigRoot
public class KomapperBuildTimeConfig {

  /** The default datasource. */
  @ConfigItem(name = ConfigItem.PARENT)
  public DataSourceBuildTimeConfig defaultDataSource;

  /** Additional named datasources. */
  @ConfigDocSection
  @ConfigDocMapKey("datasource-name")
  @ConfigItem(name = ConfigItem.PARENT)
  public Map<String, DataSourceBuildTimeConfig> namedDataSources;

  @SuppressWarnings("CanBeFinal")
  @ConfigGroup
  public static class DataSourceBuildTimeConfig {

    /** The batch size. */
    @ConfigItem(defaultValue = "0")
    public int batchSize = 0;

    /** The fetch size. */
    @ConfigItem(defaultValue = "0")
    public int fetchSize = 0;

    /** The max rows. */
    @ConfigItem(defaultValue = "0")
    public int maxRows = 0;

    /** The query timeout limit in seconds. */
    @ConfigItem(defaultValue = "0")
    public int queryTimeout = 0;
  }
}
