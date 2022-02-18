package org.komapper.gradle;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.komapper.gradle.codegen.Generator;

import java.util.Objects;

public class KomapperExtension {
    public static final String NAME = "komapper";
    private final NamedDomainObjectContainer<Generator> generators;

    public KomapperExtension(Project project) {
        Objects.requireNonNull(project);
        this.generators =
                project.container(
                        Generator.class,
                        name -> project.getObjects().newInstance(Generator.class, name, project));
    }

    public NamedDomainObjectContainer<Generator> getGenerators() {
        return generators;
    }

    @SuppressWarnings("unused")
    public void generators(Action<? super NamedDomainObjectContainer<Generator>> action) {
        action.execute(generators);
    }
}
