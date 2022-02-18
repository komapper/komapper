package org.komapper.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.komapper.gradle.codegen.GenerateTask;

public class KomapperPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    KomapperExtension extension =
        project.getExtensions().create(KomapperExtension.NAME, KomapperExtension.class, project);
    registerGenerateTasks(project, extension);
  }

  private void registerGenerateTasks(Project project, KomapperExtension extension) {
    String description = "Generate entities and definitions";
    String prefix = KomapperExtension.NAME;
    String suffix = "Generator";
    TaskProvider<?> aggregateTask =
        project
            .getTasks()
            .register(
                prefix + suffix,
                it -> {
                  it.setDescription(description + ".");
                  it.setGroup(KomapperExtension.NAME);
                  it.getOutputs().upToDateWhen(__ -> false);
                });
    extension
        .getGenerators()
        .all(
            generator -> {
              String name = prefix + capitalize(generator.getName()) + suffix;
              TaskProvider<GenerateTask> task =
                  project.getTasks().register(name, GenerateTask.class, generator);
              task.configure(
                  it -> {
                    it.setDescription(description + " for " + generator.getName() + ".");
                    it.setGroup(KomapperExtension.NAME);
                    it.getOutputs().upToDateWhen(__ -> false);
                  });
              aggregateTask.configure(it -> it.dependsOn(task));
            });
  }

  private String capitalize(String text) {
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }
}
