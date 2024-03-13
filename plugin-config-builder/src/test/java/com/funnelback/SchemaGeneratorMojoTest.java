package com.funnelback;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.junit5.InjectMojo;
import org.apache.maven.plugin.testing.junit5.MojoTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MojoTest
public class SchemaGeneratorMojoTest {

    @Test
    @InjectMojo(goal = "schema-generator", pom = "file:src/test/resources/unit/p1/pom.xml")
    public void testNoPackageName(SchemaGeneratorMojo mojo) {
        Assertions.assertNotNull(mojo);
        Assertions.assertThrows(MojoFailureException.class, mojo::execute);
    }

    @Test
    @InjectMojo(goal = "schema-generator", pom = "file:src/test/resources/unit/p2/pom.xml")
    public void testPackageNameNoClass(SchemaGeneratorMojo mojo) {
        Assertions.assertNotNull(mojo);
        Assertions.assertThrows(MojoExecutionException.class, mojo::execute);
    }
}