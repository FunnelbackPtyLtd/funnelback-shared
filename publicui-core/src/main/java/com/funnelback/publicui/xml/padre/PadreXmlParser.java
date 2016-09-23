package com.funnelback.publicui.xml.padre;

import java.nio.charset.Charset;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlParsingException;

/**
 * Parses PADRE XML response.
 */
public interface PadreXmlParser {

    /**
     * Parses PADRE XML, possibly allowing content in the prolog 
     * @param xml XML to parse
     * @param skipCommentInProlog Whether to skip comments in prolog instead of
     * throwing a parsing exception. This is usually used in conjunction with debug
     * mode for padre-sw.
     * @return Parsed {@link ResultPacket}
     * @throws XmlParsingException If the XML is malformed
     */
    public ResultPacket parse(byte[] padreStdOut, 
        Charset charset,
        boolean skipCommentInProlog) throws XmlParsingException;
    
}
