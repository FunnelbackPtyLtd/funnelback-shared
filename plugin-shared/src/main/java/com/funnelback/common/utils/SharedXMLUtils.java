package com.funnelback.common.utils;

import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * XML utility routines.
 * @author msheppard@funnelback.com
 */
@Log4j2
public class SharedXMLUtils {

    // Private constructor so that this static utility class cannot be instantiated
    private SharedXMLUtils() {}

    /** XML file extension */
    public static final String XML = "xml";

    private static TransformerFactory tf = TransformerFactory.newInstance();

    /**
     * Note that:
     * An object of this class may not be used in multiple threads
     * hence do not cache the result.
     * @param encoding
     * @return
     */
    public static Transformer getTransformer(String encoding) {
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        return transformer;
    }

    /**
     *
     * @param is
     * @return
     * @throws IllegalArgumentException when the XML from the input source is bad.
     * @throws RuntimeException when somethnig is wrong with the parser itself.
     */
    public static Document fromInputSource(InputSource is)
            throws IllegalArgumentException, RuntimeException{
        try {
            return documentBuilder().parse(is);
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Document fromInputStream(InputStream is)
            throws IllegalArgumentException, RuntimeException{
        try {
            return documentBuilder().parse(is);
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Document fromFile(File file) {
        Document document = null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));
            InputSource is = new InputSource(br);
            document = fromInputSource(is);
            document.setXmlStandalone(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }

        return document;
    }


    public static String toString(Document document) {
        try {
            return new String(toBytes(document, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toBytes(Document document, String charcterEncodingOfBytes){
        try {
            document.setXmlStandalone(true);
            DOMSource source = new DOMSource(document.getDocumentElement());

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream outStream = new PrintStream(output, false, charcterEncodingOfBytes);
            StreamResult result = new StreamResult(outStream);

            getTransformer(charcterEncodingOfBytes).transform(source, result);
            return output.toByteArray();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            // Should be impossible
            throw new RuntimeException(e);
        }
    }

    public static String toString(String xml) {
        Document document = fromString(xml);
        return toString(document);
    }

    public static Document fromString(String xmlString) {
        return fromInputSource(new InputSource(new StringReader(xmlString)));
    }

    public static Document fromBytes(byte[] xml) {
        return fromInputSource(new InputSource(new ByteArrayInputStream(xml)));
    }

    private static DocumentBuilder documentBuilder() {
        try {
            // Copied from:
            // https://github.com/mattsheppard/PfxWebDAVServer/blob/master/src/main/java/nl/ellipsis/webdav/server/util/XMLHelper.java
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            Collection<String> XML_FEATURES_TO_DISABLE = List.of(
                    // Features from https://xerces.apache.org/xerces-j/features.html
                    "http://xml.org/sax/features/external-general-entities",
                    "http://xml.org/sax/features/external-parameter-entities",
                    "http://apache.org/xml/features/validation/schema",
                    "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",

                    // Features from https://xerces.apache.org/xerces2-j/features.html
                    "http://apache.org/xml/features/xinclude/fixup-base-uris"
            );

            documentBuilderFactory.setExpandEntityReferences(false);

            // Set the validating off because it can be mis-used to pull a validation document
            // that is malicious or from the local machine
            documentBuilderFactory.setValidating(false);

            documentBuilderFactory.setXIncludeAware(false);

            // If this is set true then documents that turn up with a <!DOCTYPE will be ones that we can not process
            // I don't think that is acceptable, I think we need to still crawl and serve them but we need to now load outside
            // documents.
            // documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // https://owasp.trendmicro.com/main#!/codeBlocks/disableXmlExternalEntities has these
            // though set to false, but I think they're only possible to set if we have some additional
            // xerces and jaxp-api stuff on the classpath for them to be available/relevant
            // As it is, they all throw `ParserConfigurationException: Feature 'X' is not recognized.`
            //
            // https://stackoverflow.com/a/58522022/797 is the closest I found to discussion of it.
            //
            // documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            // documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            for (String feature : XML_FEATURES_TO_DISABLE) {
                documentBuilderFactory.setFeature(feature, false);
            }

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            documentBuilder.setEntityResolver(NOOP_ENTITY_RESOLVER);
            return documentBuilder;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static final NoOpEntityResolver NOOP_ENTITY_RESOLVER = new NoOpEntityResolver();

    private static class NoOpEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }
    }

}
