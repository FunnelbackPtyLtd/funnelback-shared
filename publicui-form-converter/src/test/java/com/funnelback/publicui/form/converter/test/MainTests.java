package com.funnelback.publicui.form.converter.test;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.funnelback.publicui.form.converter.Main;

public class MainTests {

	@Test
	@Ignore
	public void test() throws IOException {
		File in = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "simple.form.dist");
		File expected = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "simple.ftl.dist");
		File out = File.createTempFile(MainTests.class.getName(), "tmp");
		out.deleteOnExit();
		
		String[] args = {
				"-i ", in.getAbsolutePath(),
				"-o ", out.getAbsolutePath()
		};
		
		Main.main(args);
		
		Assert.assertEquals(
				FileUtils.readFileToString(expected),
				FileUtils.readFileToString(out)
				);
	}
	
}
