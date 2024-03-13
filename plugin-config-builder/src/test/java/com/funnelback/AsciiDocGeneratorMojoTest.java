package com.funnelback;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.junit5.InjectMojo;
import org.apache.maven.plugin.testing.junit5.MojoTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MojoTest
public class AsciiDocGeneratorMojoTest {

    @Test
    @InjectMojo(goal = "ascii-doc-generator", pom = "file:src/test/resources/unit/p1/pom.xml")
    public void testNoPackageName(AsciiDocGeneratorMojo mojo) {
        Assertions.assertNotNull(mojo);
        Assertions.assertThrows(MojoFailureException.class, mojo::execute);
    }

    @Test
    @InjectMojo(goal = "ascii-doc-generator", pom = "file:src/test/resources/unit/p2/pom.xml")
    public void testPackageNameNoClass(AsciiDocGeneratorMojo mojo) {
        Assertions.assertNotNull(mojo);
        Assertions.assertThrows(MojoExecutionException.class, mojo::execute);
    }
}
