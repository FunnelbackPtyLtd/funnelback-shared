package com.funnelback.utils;

public interface PanLookSeeker {

	long getStartOfLine(long position);

	String getString(long position);

	long length();

}
