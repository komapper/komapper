package org.komapper.quarkus.jdbc;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import javax.inject.Singleton;
import org.komapper.core.ClockProvider;
import org.komapper.core.DefaultClockProvider;
import org.komapper.core.ExecutionOptions;
import org.komapper.core.Logger;
import org.komapper.core.LoggerFacade;
import org.komapper.core.LoggerFacades;
import org.komapper.core.Loggers;
import org.komapper.core.StatementInspector;
import org.komapper.core.StatementInspectors;

@Singleton
@Unremovable
public class KomapperProducer {

  @Singleton
  @DefaultBean
  @Unremovable
  public ClockProvider clockProvider() {
    return new DefaultClockProvider();
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public ExecutionOptions executionOptions() {
    return new ExecutionOptions();
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public Logger logger() {
    return Loggers.INSTANCE.get();
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public LoggerFacade loggerFacade(Logger logger) {
    return LoggerFacades.INSTANCE.get(logger);
  }

  @Singleton
  @DefaultBean
  @Unremovable
  public StatementInspector statementInspector() {
    return StatementInspectors.INSTANCE.get();
  }
}
