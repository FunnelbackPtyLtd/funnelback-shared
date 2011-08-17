package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;

public class StandaloneParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.out.println("You must provide the folder containing XML files");
			System.exit(1);
		}
		
		File dir = new File(args[0]);
		if (!dir.isDirectory()) {
			System.out.println("Provided path '" + dir.getAbsolutePath() + "' is not a directory");
			System.exit(1);
		}
		System.out.println("Using folder: '" + dir.getAbsolutePath() + "'");
		
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});

		configureLogging();
		
		int errors = 0;
		StaxStreamParser parser = new StaxStreamParser();
		for (File f: files) {
			try {
				parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml"), "UTF-8"));
			} catch (Exception e) {
				System.out.println("Error while parsing '" + f.getAbsolutePath() + "'");
				e.printStackTrace();
				errors++;
			}
		}
		
		if (errors > 0) {
			System.out.println(errors + " errors encountered over " + files.length + " files");
			System.exit(1);
		} else {
			System.out.println("All " + files.length + " XML files were parsed successfully");
		}
		
	}
	
	private static void configureLogging() {
		Properties p = new Properties();
		p.put("log4j.rootLogger", "DEBUG, console");
		p.put("log4j.appender.console", ConsoleAppender.class.getName());
		p.put("log4j.appender.console.layout", PatternLayout.class.getName());
		p.put("log4j.appender.console.layout.ConversionPattern", "%d [%t] %-5p %c{2} %x - %m%n");
		
		PropertyConfigurator.configure(p);
	}

}
