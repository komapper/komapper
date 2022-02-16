package org.komapper.gradle.codegen;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Objects;

public class Jdbc {
  private final Property<String> driver;
  private final Property<String> url;
  private final Property<String> user;
  private final Property<String> password;

  @Inject
  public Jdbc(ObjectFactory objects) {
    Objects.requireNonNull(objects);
    this.driver = objects.property(String.class);
    this.url = objects.property(String.class);
    this.user = objects.property(String.class).value("");
    this.password = objects.property(String.class).value("");
  }

  public Property<String> getDriver() {
    return driver;
  }

  public Property<String> getUrl() {
    return url;
  }

  public Property<String> getUser() {
    return user;
  }

  public Property<String> getPassword() {
    return password;
  }
}
