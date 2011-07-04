package com.funnelback.utils;

import java.io.File;
import java.io.IOException;

public interface PanLookFactory {

	PanLook getPanLookForLex(File sortedFile, String word) throws IOException;

	PanLook getPanLook(File sortedFile, String prefix) throws IOException;

}
