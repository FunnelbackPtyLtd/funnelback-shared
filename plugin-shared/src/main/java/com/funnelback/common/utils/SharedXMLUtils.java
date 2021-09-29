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
 * Shared XML Utilities that can help parse documents and transform them in a safe
 * way that aims to avoid common OWASP vulnerabilities.
 */
@Log4j2
public class SharedXMLUtils {

    // Private constructor so that this static utility class cannot be instantiated
    private SharedXMLUtils() {}

    /** XML file extension */
    public static final String XML = "xml";

    private static TransformerFactory tf = TransformerFactory.newInstance();

    /**
     * Get a transformer that can be used for transforming a Document back out into an output stream.
     * Note that the returned Transformer is not threadsafe. What that means is that it should not be used in
     * multiple threads. Note: The filter framework is inherintly called by multiple crawler threads, so when using it
     * for a plugin should call this method repeatedly and use a fresh transformer each time rather than
     * getting one in the constructor for re-use.
     * @param encoding
     * @return A new transformer instance.
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
     * Parse a Document from an inputSource.
     *
     * Internally uses a custom documentBuilder instance with many security settings enabled.
     *
     * @param inputSource - e.g. new InputSource(bufferedReader)
     * @return Document - for use with a transformer, or xpath evaluation.
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

    /**
     * Parse a Document from an inputStream.
     *
     * Internally uses a custom documentBuilder instance with many security settings enabled.
     *
     * Example usage from the filter framework:
     * public FilterResult filterAsBytesDocument(BytesDocument document, FilterContext filterContext) {
     *   Document doc = SharedXMLUtils.fromInputStream(document.contentAsInputStream())
     *   // Do some processing to change or query the document.
     *   var bos = new ByteArrayOutputStream();
     *   SharedXMLUtils.getTransformer("UTF-8").transform(new DOMSource(doc), new StreamResult(bos));
     *   byte[] documentContentsAsBytes = bos.toByteArray();
     * }
     *
     * @param InputStream - document to read/parse
     * @return Document - after parsing.
     * @throws IllegalArgumentException
     * @throws RuntimeException
     */
    public static Document fromInputStream(InputStream is)
            throws IllegalArgumentException, RuntimeException{
        try {
            return documentBuilder().parse(is);
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse a Document from a given File path. Useful for testing.
     *
     * Internally uses a custom documentBuilder instance with many security settings enabled.
     *
     * @param file to parse into a Document
     * @return Document after parsing.
     */
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

    /**
     * Converts a given Document back out to a String. Useful for testing/debugging.
     *
     * @param document
     * @return
     */
    public static String toString(Document document) {
        try {
            return new String(toBytes(document, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a given Document back out to a byte array. Useful for testing/debugging.
     * @param document
     * @param charcterEncodingOfBytes - e.g. UTF-8
     * @return
     */
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

    /**
     * Round trip conversion of a given XML string back into XML with the secure transformations applied.
     * @param xml
     * @return
     */
    public static String toString(String xml) {
        Document document = fromString(xml);
        return toString(document);
    }

    /**
     * Parse a Document from a given String. Useful for testing.
     *
     * Internally uses a custom documentBuilder instance with many security settings enabled.
     *
     * @param xmlString to parse into a Document
     * @return Document after parsing.
     */
    public static Document fromString(String xmlString) {
        return fromInputSource(new InputSource(new StringReader(xmlString)));
    }

    /**
     * Parse a Document from a given byte[]. Useful for testing.
     *
     * Internally uses a custom documentBuilder instance with many security settings enabled.
     *
     * @param xml to parse into a Document
     * @return Document after parsing.
     */
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

    // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#no-op-entityresolver
    private static class NoOpEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }
    }

}
