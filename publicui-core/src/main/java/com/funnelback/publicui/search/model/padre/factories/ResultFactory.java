package com.funnelback.publicui.search.model.padre.factories;

import com.funnelback.publicui.search.model.padre.Collapsed;
import com.funnelback.publicui.search.model.padre.Explain;
import com.funnelback.publicui.search.model.padre.QuickLinks;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.xml.XmlStreamUtils;
import com.funnelback.publicui.xml.XmlStreamUtils.TagAndText;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * Builds {@link Result}s from various input sources.
 * 
 */
@Log4j2
public class ResultFactory {

    /**
     * Builds a {@link Result} from a Map containing the values
     * 
     * @param data
     *            The map containing the values
     * @param ql Quick links for the results
     * @param explain Explain data for the result
     * @param collapsed Collapsing information for the result
     * @return A result with populated values
     */
    public static Result fromMap(Map<String, String> data, QuickLinks ql, Explain explain, Collapsed collapsed) {
        Integer rank = NumberUtils.toInt(data.get(Result.Schema.RANK), 0);
        Integer score = NumberUtils.toInt(data.get(Result.Schema.SCORE), 0);
        String title = data.get(Result.Schema.TITLE);
        String collection = data.get(Result.Schema.COLLECTION);
        Integer component = NumberUtils.toInt(data.get(Result.Schema.COMPONENT), 0);
        String liveUrl = data.get(Result.Schema.LIVE_URL);
        String summary = data.get(Result.Schema.SUMMARY);
        String cacheUrl = data.get(Result.Schema.CACHE_URL);
        String exploreLink = data.get(Result.Schema.EXPLORE_LINK);
        
        boolean documentVisibleTouser = isDocumentVisibleToUser(data);
        
        // If the result is not explicitly marked promoted then it is not.
        boolean promoted = getBoolean(data, Result.Schema.PROMOTED).orElse(false);
        
        // If the result is not explicitly marked as diversified then it is not.
        boolean diversified = getBoolean(data, Result.Schema.DIVERSIFIED).orElse(false);

        String dateString = data.get(Result.Schema.DATE);
        Date date = null;
        if (dateString != null && !"".equals(dateString) && !Result.NO_DATE.equals(dateString)) {
            try {
                date = DateUtils.parseDate(dateString, Result.DATE_PATTERNS_IN);
            } catch (Exception e) {
                log.debug("Unparseable date: '" + dateString + "'");
            }
        }

        Integer fileSize = NumberUtils.toInt(data.get(Result.Schema.FILESIZE), 0);
        String fileType = data.get(Result.Schema.FILETYPE);
        Integer tier = NumberUtils.toInt(data.get(Result.Schema.TIER), 0);
        Integer documentNumber = NumberUtils.toInt(data.get(Result.Schema.DOCNUM), 0);
        
        Float kmFromOrigin = null;
        if (data.get(Result.Schema.KM_FROM_ORIGIN) != null) {
            kmFromOrigin = Float.valueOf(data.get(Result.Schema.KM_FROM_ORIGIN));
        }

        HashMap<String, String> metadataMap = new HashMap<String, String>();
        for (String key : data.keySet()) {
            if (key.startsWith(Result.METADATA_PREFIX)) {
                metadataMap.put(key.substring(Result.METADATA_PREFIX.length()), data.get(key));
            }
        }

        Set<String> gscopesSet = parseGScopeSet(data.get(Result.Schema.GSCOPES_SET));

        Result r = new Result(
                rank,
                score,
                title,
                collection,
                component,
                collapsed,
                liveUrl,
                summary,
                cacheUrl,
                date,
                fileSize,
                fileType,
                tier,
                documentNumber,
                exploreLink,
                kmFromOrigin,
                ql,
                liveUrl,
                liveUrl,
                explain,
                liveUrl,
                gscopesSet,
                documentVisibleTouser,
                promoted,
                diversified);

        r.getMetaData().putAll(metadataMap);
        if (data.get(Result.Schema.TAGS) != null) {
            r.getTags().addAll(Arrays.asList(data.get(Result.Schema.TAGS).split(",")));
        }

        return r;
    }
    
    static boolean isDocumentVisibleToUser(Map<String, String> data) {
        // By default documents are visible
        return getBoolean(data, Result.Schema.DOCUMENT_VISIBLE_TO_USER).orElse(true);
    }
    
    
    static Optional<Boolean> getBoolean(Map<String, String> data, String tag) {
        if(data.get(tag) != null) {
             return Optional.of(Boolean.parseBoolean(data.get(tag)));
        }
        return Optional.empty();
    }
    
    

    /** Parses the <gscopes_set> field into a Set of Integers
     *  If it hits any failure it will return the set of as
     *  many as it parsed, or the empty set.
     * */
    private static Set<String> parseGScopeSet(String strGScopesSet) {
        if(strGScopesSet == null || strGScopesSet.trim().length() == 0) {
            return new HashSet<>();
        }

        try {
            //Split on commas
            return new HashSet<>(Arrays.asList(strGScopesSet.split(",")));
        } catch (PatternSyntaxException pse) {
            return new HashSet<>();
        }
    }

    /**
     * Builds a {@link Result} from an {@link XMLStreamReader} Doesn't supports
     * MetaData reading. Assumes that the stream is already inside the <result>
     * tag
     * 
     * @param xmlStreamReader
     *            The {@link XMLStreamReader} to read from
     * @return A Result with populated values
     * @throws XMLStreamException
     */
    public static Result fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if (!Result.Schema.RESULT.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }

        Map<String, String> data = new HashMap<String, String>();
        QuickLinks ql = null;
        Explain explain = null;
        Collapsed collapsed = null;
        
        while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
            if (xmlStreamReader.isStartElement()) {
                if (Result.Schema.METADATA.equals(xmlStreamReader.getLocalName())) {
                    // Specific case for metadtata <md f="x">value</md>
                    String mdClass = xmlStreamReader.getAttributeValue(null, Result.Schema.ATTR_METADATA_F);
                    String value = xmlStreamReader.getElementText();
                    data.put(Result.METADATA_PREFIX + mdClass, value);
                } else if (QuickLinks.Schema.QUICKLINKS.equals(xmlStreamReader.getLocalName())) {
                    ql = QuickLinksFactory.fromXmlStreamReader(xmlStreamReader);
                } else if(Result.Schema.EXPLAIN.equals(xmlStreamReader.getLocalName())) {
                    explain = ExplainFactory.fromXmlStreamReader(xmlStreamReader);
                } else if (Result.Schema.TAGS.equals(xmlStreamReader.getLocalName())) {
                    data.put(Result.Schema.TAGS, parseTags(xmlStreamReader));
                } else if (Result.Schema.COLLAPSED.equals(xmlStreamReader.getLocalName())) {
                    collapsed  = parseCollapsed(xmlStreamReader);
                } else {
                    TagAndText tt = XmlStreamUtils.getTagAndValue(xmlStreamReader);
                    data.put(tt.tag, tt.text);
                }
            }
        }

        return fromMap(data, ql, explain, collapsed);
    }
    
    /**
     * Parses tags associated with a results. <tt>href</tt> attributes are ignored because the URL
     * will be rebuilt manually if needed.
     * @param reader
     * @return
     * @throws XMLStreamException
     */
    private static String parseTags(XMLStreamReader reader) throws XMLStreamException {
        if (! Result.Schema.TAGS.equals(reader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        List<String> tags = new ArrayList<String>();
        
        int type = reader.getEventType();
        while (type != XMLStreamReader.END_ELEMENT || ! Result.Schema.TAGS.equals(reader.getLocalName())) {
            type = reader.nextTag();
            
            if (type == XMLStreamReader.START_ELEMENT && Result.Schema.RQ.equals(reader.getLocalName())) {
                tags.add(reader.getElementText());
            }
        }
        
        return StringUtils.join(tags, ",");
    }
    
    private static Collapsed parseCollapsed(XMLStreamReader reader) throws XMLStreamException {
        if (! Result.Schema.COLLAPSED.equals(reader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        String signature = reader.getAttributeValue(null, Result.Schema.COLLAPSED_SIG);
        String column = reader.getAttributeValue(null, Result.Schema.COLLAPSED_COL);
        String count = reader.getAttributeValue(null, Result.Schema.COLLAPSED_COUNT);

        Collapsed collapsed = new Collapsed(signature,
                Integer.parseInt(count),
                column);

        // Parse collapsed results, if any
        int type = reader.getEventType();
        while (type != XMLStreamReader.END_ELEMENT || ! Result.Schema.COLLAPSED.equals(reader.getLocalName())) {
            type = reader.next();
            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    if (Result.Schema.RESULT.equals(reader.getLocalName())) {
                        collapsed.getResults().add(ResultFactory.fromXmlStreamReader(reader));
                    }
                    break;
            }
        }

        return collapsed;
 
    }

}
