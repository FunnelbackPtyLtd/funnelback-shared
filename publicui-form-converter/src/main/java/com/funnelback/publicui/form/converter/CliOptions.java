package com.funnelback.publicui.form.converter;

import java.io.File;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

public class CliOptions {

	@Parameter(names={"-i", "--input"}, description="Input form file", required=true)
	public File input;
	
	@Parameter(names={"-o", "--output"}, description="Output form file", required=true)
	public File output;
	
	@Parameter(names={"-v"}, description="Enable verbose output", required=false)
	public boolean verbose = false;
	
	@Parameter(names={"-vv"}, description="Enable more verbose output", required=false)
	public boolean veryVerbose = false;
	
	
	public class FileConverter implements IStringConverter<File> {
		@Override
		public File convert(String s) {
			return new File(s);
		}
		
	}
	
}
