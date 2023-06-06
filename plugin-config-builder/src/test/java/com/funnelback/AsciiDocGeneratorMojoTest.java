package com.funnelback;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

public class AsciiDocGeneratorMojoTest {
    @Rule public MojoRule rule = new MojoRule();
    @Rule public TestResources resources = new TestResources("src/test/resources/unit", "target/test-harness");

    @Test
    public void testNoPackageName() throws Exception {
        AsciiDocGeneratorMojo mojo = getMojo("p1");
        Assert.assertThrows(MojoFailureException.class, mojo::execute);
    }

    @Test
    public void testPackageNameNoClass() throws Exception {
        AsciiDocGeneratorMojo mojo = getMojo("p2");
        Assert.assertThrows(MojoExecutionException.class, mojo::execute);
    }

    private AsciiDocGeneratorMojo getMojo(String projectName) throws Exception {
        File projectDir = resources.getBasedir(projectName);
        AsciiDocGeneratorMojo mojo = (AsciiDocGeneratorMojo) rule.lookupMojo("ascii-doc-generator", getPom(projectDir));
        mojo.project = rule.readMavenProject(projectDir);
        Assert.assertNotNull(mojo);
        return mojo;
    }

    private File getPom(File projectDir) {
        File pom = new File(projectDir, "pom.xml");
        Assert.assertTrue(pom.exists());
        return pom;
    }
}
