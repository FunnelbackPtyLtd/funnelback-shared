package com.funnelback.utils;

import java.io.IOException;

public interface PanLook extends Iterable<String> {

	void close() throws IOException;
}
