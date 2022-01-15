package org.komapper.quarkus.jdbc;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.Objects;

public interface DataSourceResolver {
    DataSource resolve(String dataSourceName);
}
