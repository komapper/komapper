package org.komapper.quarkus.jdbc.deployment;

import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;
import java.util.Map;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.komapper")
public interface KomapperBuildTimeConfig {

  /** DataSources. */
  @ConfigDocSection
  @ConfigDocMapKey("datasource-name")
  @WithParentName
  @WithUnnamedKey(DataSourceUtil.DEFAULT_DATASOURCE_NAME)
  @WithDefaults
  Map<String, DataSourceBuildTimeConfig> dataSources();

  @SuppressWarnings("CanBeFinal")
  @ConfigGroup
  interface DataSourceBuildTimeConfig {

    /** The batch size. */
    @WithDefault("0")
    int batchSize();

    /** The fetch size. */
    @WithDefault("0")
    int fetchSize();

    /** The max rows. */
    @WithDefault("0")
    int maxRows();

    /** The query timeout limit in seconds. */
    @WithDefault("0")
    int queryTimeout();
  }
}
