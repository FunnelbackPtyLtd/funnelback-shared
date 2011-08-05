package com.funnelback.utils;

import java.io.File;
import java.io.IOException;

public class MemoryMappedLineSeekerForLex extends MemoryMappedLineSeeker {

	public MemoryMappedLineSeekerForLex(File sortedFile) throws IOException {
		super(sortedFile,new byte[] {'\n'});
	}
	
	@Override
	public String getString(long position) {
		return super.getString(position).replaceAll("_", " ");
	}

}
	