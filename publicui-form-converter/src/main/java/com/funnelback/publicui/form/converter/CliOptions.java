package com.funnelback.publicui.form.converter;

import java.io.File;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

public class CliOptions {

	@Parameter(names={"-i", "--input"}, description="Input form file", required=true)
	public File input;
	
	@Parameter(names={"-o", "--output"}, description="Output form file", required=true)
	public File output;
	
	
	public class FileConverter implements IStringConverter<File> {
		@Override
		public File convert(String s) {
			return new File(s);
		}
		
	}
	
}
