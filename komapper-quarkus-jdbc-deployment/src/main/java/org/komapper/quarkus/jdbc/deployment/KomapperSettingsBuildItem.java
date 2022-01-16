package org.komapper.quarkus.jdbc.deployment;

import io.quarkus.builder.item.SimpleBuildItem;
import java.util.Objects;
import org.komapper.quarkus.jdbc.KomapperSettings;

public final class KomapperSettingsBuildItem extends SimpleBuildItem {

  private final KomapperSettings settings;

  public KomapperSettingsBuildItem(KomapperSettings settings) {
    this.settings = Objects.requireNonNull(settings);
  }

  public KomapperSettings getSettings() {
    return settings;
  }
}
