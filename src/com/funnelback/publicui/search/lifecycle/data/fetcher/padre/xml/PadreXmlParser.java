package com.funnelback.publicui.search.lifecycle.data.fetcher.padre.xml;

import com.funnelback.publicui.search.model.padre.ResultPacket;

/**
 * Parses PADRE XML output.
 */
public interface PadreXmlParser {

	public ResultPacket parse(String xml) throws PadreXmlParsingException;
	
}
