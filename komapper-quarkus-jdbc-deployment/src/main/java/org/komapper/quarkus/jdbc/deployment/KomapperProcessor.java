package org.komapper.quarkus.jdbc.deployment;

import io.quarkus.agroal.DataSource;
import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.enterprise.inject.Default;
import javax.inject.Singleton;
import org.jboss.jandex.DotName;
import org.komapper.jdbc.JdbcDatabase;
import org.komapper.jdbc.JdbcDatabaseConfig;
import org.komapper.quarkus.jdbc.DataSourceDefinition;
import org.komapper.quarkus.jdbc.DefaultDataSourceResolver;
import org.komapper.quarkus.jdbc.KomapperProducer;
import org.komapper.quarkus.jdbc.KomapperRecorder;

public class KomapperProcessor {
  public static final String FEATURE = "komapper";

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  AdditionalBeanBuildItem additionalBeans() {
    return new AdditionalBeanBuildItem(KomapperProducer.class, DefaultDataSourceResolver.class);
  }

  @BuildStep
  KomapperSettingsBuildItem komapperSettings(
      KomapperBuildTimeConfig buildTimeConfig, List<JdbcDataSourceBuildItem> dataSources) {
    var factory = new KomapperSettingsFactory(buildTimeConfig, dataSources);
    return new KomapperSettingsBuildItem(factory.create());
  }

  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  void registerBeans(
      KomapperRecorder recorder,
      KomapperSettingsBuildItem komapperSettings,
      BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

    var settings = komapperSettings.getSettings();

    registerSyntheticBeans(
        settings.dataSourceDefinitions,
        syntheticBeans,
        JdbcDatabaseConfig.class,
        JdbcDatabaseConfig.class,
        recorder::configureJdbcDatabaseConfig);

    registerSyntheticBeans(
        settings.dataSourceDefinitions,
        syntheticBeans,
        JdbcDatabase.class,
        JdbcDatabase.class,
        recorder::configureJdbcDatabase);
  }

  private <BEAN> void registerSyntheticBeans(
      List<DataSourceDefinition> dataSourceDefinitions,
      BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
      Class<? extends BEAN> implClazz,
      Class<BEAN> typeClazz,
      Function<DataSourceDefinition, Supplier<BEAN>> supplierCreator) {
    dataSourceDefinitions.stream()
        .map(
            dataSourceDefinition -> {
              SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator =
                  SyntheticBeanBuildItem.configure(implClazz)
                      .addType(DotName.createSimple(typeClazz.getName()))
                      .scope(Singleton.class)
                      .unremovable()
                      .supplier(supplierCreator.apply(dataSourceDefinition));
              if (dataSourceDefinition.isDefault) {
                configurator.addQualifier().annotation(Default.class).done();
              } else {
                configurator
                    .addQualifier()
                    .annotation(DataSource.class)
                    .addValue("value", dataSourceDefinition.name)
                    .done();
              }
              return configurator.done();
            })
        .forEach(syntheticBeans::produce);
  }
}
