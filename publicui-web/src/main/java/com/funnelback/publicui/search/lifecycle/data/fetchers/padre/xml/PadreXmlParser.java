package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlParsingException;

/**
 * Parses PADRE XML response.
 */
public interface PadreXmlParser {

	public ResultPacket parse(String xml) throws XmlParsingException;
	
}
