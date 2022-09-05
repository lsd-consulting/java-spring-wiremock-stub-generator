package io.lsdconsulting.wiremock.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WiremockStubJarPluginShould {

    @Test
    void addCompileStubsTask() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("io.github.lsd-consulting.gradle.wiremock-stub-jar");

        Task compileStubsTask = project.getTasks().getByName("compileStubs");
        assertTrue(compileStubsTask instanceof JavaCompile);
        assertThat(((JavaCompile) compileStubsTask).getDestinationDirectory().get().toString(), endsWith("/build/generated-stub-classes"));
    }

    @Test
    void addStubsJarTask() {
        Project project = ProjectBuilder.builder().withName("MyProjectName").build();
        project.getPluginManager().apply("io.github.lsd-consulting.gradle.wiremock-stub-jar");

        Task stubsJarTask = project.getTasks().getByName("stubsJar");
        assertTrue(stubsJarTask instanceof Jar);
        assertThat(stubsJarTask.getGroup(), is("Verification"));
        assertThat(((Jar) stubsJarTask).getArchiveBaseName().get(), is("MyProjectName"));
        assertThat(((Jar) stubsJarTask).getArchiveClassifier().get(), is("wiremock-stubs"));
        assertThat(((Jar) stubsJarTask).getDestinationDirectory().get().toString(), endsWith("/build/libs"));
    }
}
