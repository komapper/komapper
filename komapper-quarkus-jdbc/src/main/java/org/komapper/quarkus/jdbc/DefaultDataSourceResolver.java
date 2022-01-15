package org.komapper.quarkus.jdbc;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.Objects;

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
