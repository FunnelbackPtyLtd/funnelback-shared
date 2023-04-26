package com.funnelback;

import com.funnelback.plugin.PluginUtilsBase;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
@Mojo(
    name = "schema-generator",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.COMPILE
)
public class SchemaGeneratorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "packageName", required = true)
    private String packageName;

    @Parameter(property = "resourcesPath", defaultValue = "${project.build.resources[0].directory}", readonly = true)
    private String resourcesPath;

    @Override public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Generating plugin schema in JSON format");

        if (packageName == null || packageName.isBlank()) {
            throw new MojoFailureException("No package name containing PluginUtils class was provided");
        }

        try {
            Class<?> clazz = getClassLoader(project).loadClass(packageName + ".PluginUtils");
            PluginUtilsBase pluginUtils = (PluginUtilsBase) clazz.getDeclaredConstructor().newInstance();
            new SchemaGenerator(pluginUtils, resourcesPath).generate();
        } catch (ClassNotFoundException | IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            getLog().error("Failed to execute plugin");
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private ClassLoader getClassLoader(MavenProject project) {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            classpathElements.add(project.getBuild().getOutputDirectory());
            URL[] urls = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                urls[i] = new File(classpathElements.get(i)).toURI().toURL();
            }
            return new URLClassLoader(urls, this.getClass().getClassLoader());
        }
        catch (Exception e) {
            getLog().warn( "Couldn't get the classloader");
            return this.getClass().getClassLoader();
        }
    }

}
