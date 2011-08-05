package com.funnelback.utils;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class DefaultPanLookFactory implements PanLookFactory{

	@Override
	public PanLook getPanLookForLex(File sortedFile,String word) throws IOException {
		return new MemoryMapperPanLook(new MemoryMappedLineSeekerForLex(sortedFile), word + " ");
	}
	
	@Override
	public PanLook getPanLook(File sortedFile,String prefix) throws IOException {
		 return new MemoryMapperPanLook(new MemoryMappedLineSeeker(sortedFile, System.getProperty("line.separator").getBytes()), prefix);
	}

}
