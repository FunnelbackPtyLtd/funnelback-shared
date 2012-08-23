package com.funnelback.utils;

import java.io.IOException;

public interface PanLookSeeker {

	long getStartOfLine(long position);

	String getString(long position);

	long length();

	long getSizeOfLineSep();
	
	void close() throws IOException;

}
