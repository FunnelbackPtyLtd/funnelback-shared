package com.funnelback.publicui.xml.padre;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.search.model.padre.ContextualNavigation;
import com.funnelback.publicui.search.model.padre.CoolerWeighting;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.model.padre.Error;
import com.funnelback.publicui.search.model.padre.QSup;
import com.funnelback.publicui.search.model.padre.QSup.Source;
import com.funnelback.publicui.search.model.padre.RMCItemResult;
import com.funnelback.publicui.search.model.padre.Range;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.padre.Spell;
import com.funnelback.publicui.search.model.padre.TierBar;
import com.funnelback.publicui.search.model.padre.factories.BestBetFactory;
import com.funnelback.publicui.search.model.padre.factories.ContextualNavigationFactory;
import com.funnelback.publicui.search.model.padre.factories.DetailsFactory;
import com.funnelback.publicui.search.model.padre.factories.ErrorFactory;
import com.funnelback.publicui.search.model.padre.factories.RMCItemResultFactory;
import com.funnelback.publicui.search.model.padre.factories.ResultFactory;
import com.funnelback.publicui.search.model.padre.factories.ResultsSummaryFactory;
import com.funnelback.publicui.search.model.padre.factories.SpellFactory;
import com.funnelback.publicui.search.model.padre.factories.TierBarFactory;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.XmlStreamUtils;

@Log4j
public class StaxStreamParser implements PadreXmlParser {

    /** Tag marking the start of the XML document */
    private static final String XML_HEADER_TAG = "<?xml";
    
    /** Regexp to match everything before the XML start tag */
    private static final Pattern XML_PROLOG_PATTERN = Pattern.compile("^(.*)"+Pattern.quote(XML_HEADER_TAG),
        Pattern.DOTALL);
    
    @Override
    public ResultPacket parse(String xml, boolean allowContentInProlog) throws XmlParsingException {

        String xmlToParse = xml;
        if (allowContentInProlog) {
            xmlToParse = XML_PROLOG_PATTERN.matcher(xmlToParse).replaceFirst(XML_HEADER_TAG);
        }
        
        ResultPacket packet = new ResultPacket();
        
        try {
            XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xmlToParse));
        
            while(xmlStreamReader.hasNext()) {
                int type = xmlStreamReader.next();
                
                switch(type){
                case XMLStreamReader.START_ELEMENT:
                    
                    if (ResultPacket.Schema.PADRE_RESULT_PACKET.equals(xmlStreamReader.getLocalName())){
                        // Do nothing, root element
                    } else if (Details.Schema.DETAILS.equals(xmlStreamReader.getLocalName())) {
                        packet.setDetails(DetailsFactory.fromXmlStreamReader(xmlStreamReader));
                    } else if (ResultPacket.Schema.QUERY.equals(xmlStreamReader.getLocalName())) {
                        packet.setQuery(xmlStreamReader.getElementText());
                        packet.setQueryCleaned(packet.getQuery());
                    } else if (ResultPacket.Schema.QUERY_AS_PROCESSED.equals(xmlStreamReader.getLocalName())) {
                        packet.setQueryAsProcessed(xmlStreamReader.getElementText());
                    } else if (ResultPacket.Schema.QUERY_RAW.equals(xmlStreamReader.getLocalName())) {
                        packet.setQueryRaw(xmlStreamReader.getElementText());
                    } else if (ResultPacket.Schema.COLLECTION.equals(xmlStreamReader.getLocalName())) {
                        packet.setCollection(xmlStreamReader.getElementText());
                    } else if (ResultPacket.Schema.QSUP.equals(xmlStreamReader.getLocalName())) {
                        packet.getQSups().add(parseQSup(xmlStreamReader));
                    } else if (ResultPacket.Schema.PADRE_ELAPSED_TIME.equals(xmlStreamReader.getLocalName())) {
                        packet.setPadreElapsedTime(Integer.parseInt(xmlStreamReader.getElementText()));
                    } else if (ResultPacket.Schema.PHLUSTER_ELAPSED_TIME.equals(xmlStreamReader.getLocalName())) {
                        packet.setPhlusterElapsedTime(parsePhlusterElapsedTime(xmlStreamReader.getElementText()));
                    } else if (ResultPacket.Schema.QUERY_PROCESSOR_CODES.equals(xmlStreamReader.getLocalName())) {
                        packet.setQueryProcessorCodes(xmlStreamReader.getElementText());
                    } else if (ResultsSummary.Schema.RESULTS_SUMMARY.equals(xmlStreamReader.getLocalName())) {
                        packet.setResultsSummary(ResultsSummaryFactory.fromXmlStreamReader(xmlStreamReader));
                    } else if (Spell.Schema.SPELL.equals(xmlStreamReader.getLocalName())) {
                        packet.setSpell(SpellFactory.fromXmlStreamReader(xmlStreamReader));
                    } else if (ResultPacket.Schema.BEST_BETS.equals(xmlStreamReader.getLocalName())) {
                        packet.getBestBets().addAll(BestBetFactory.listFromXmlStreamReader(xmlStreamReader));
                    } else if (ResultPacket.Schema.RESULTS.equals(xmlStreamReader.getLocalName())) {
                        parseResults(xmlStreamReader, packet);
                    } else if (Error.Schema.ERROR.equals(xmlStreamReader.getLocalName())) {
                        packet.setError(ErrorFactory.fromXmlStreamReader(xmlStreamReader));
                    } else if (ResultPacket.Schema.RMC.equals(xmlStreamReader.getLocalName())) {
                        RMC rmc = parseRmc(xmlStreamReader);
                        packet.getRmcs().put(rmc.item, rmc.count);
                    } else if (ResultPacket.Schema.RMC_ITEM_RESULTS.equals(xmlStreamReader.getLocalName())){
                        RMC rmc = parseRmcFromRmcItemResults(xmlStreamReader);
                        packet.getRmcs().put(rmc.item, rmc.count);
                        packet.getRmcItemResults().put(rmc.item, parseRmcItemResults(xmlStreamReader));
                    } else if (ResultPacket.Schema.METADATA_RANGE.equals(xmlStreamReader.getLocalName())){
                        MetadataRange metadataRange = parseMetadataRange(xmlStreamReader);
                        packet.getMetadataRanges().put(metadataRange.field, new Range(metadataRange.min, metadataRange.max));
                    } else if (ResultPacket.Schema.URLCOUNT.equals(xmlStreamReader.getLocalName())) {
                        URLCount urlCount = parseURLCount(xmlStreamReader);
                        packet.getUrlCounts().put(urlCount.url, urlCount.count);
                    } else if (ResultPacket.Schema.GSCOPE_COUNTS.equals(xmlStreamReader.getLocalName())) {
                        packet.getGScopeCounts().putAll(parseGScopeCounts(xmlStreamReader));
                    } else if (ResultPacket.Schema.DATECOUNT.equals(xmlStreamReader.getLocalName())) {
                        DateCount dateCount = parseDateCount(xmlStreamReader);
                        packet.getDateCounts().put(dateCount.item,
                                new com.funnelback.publicui.search.model.padre.DateCount(dateCount.qTerm, dateCount.count));
                    } else if (ResultPacket.Schema.QHLRE.equals(xmlStreamReader.getLocalName())) {
                        packet.setQueryHighlightRegex(xmlStreamReader.getElementText());
                    } else if (ResultPacket.Schema.ORIGIN.equals(xmlStreamReader.getLocalName())) {
                        packet.setOrigin(parseOrigin(xmlStreamReader.getElementText()));
                    } else if (ResultPacket.Schema.ENTITYLIST.equals(xmlStreamReader.getLocalName())) {
                        packet.getEntityList().putAll(parseEntityList(xmlStreamReader));
                    } else if (ContextualNavigation.Schema.CONTEXTUAL_NAVIGATION.equals(xmlStreamReader.getLocalName())) {
                        packet.setContextualNavigation(ContextualNavigationFactory.fromXmlStreamReader(xmlStreamReader));
                    } else if (ResultPacket.Schema.INCLUDE_SCOPE.equals(xmlStreamReader.getLocalName())) {
                        packet.getIncludeScopes().addAll(parseScopes(xmlStreamReader.getElementText()));
                    } else if (ResultPacket.Schema.EXCLUDE_SCOPE.equals(xmlStreamReader.getLocalName())) {
                        packet.getExcludeScopes().addAll(parseScopes(xmlStreamReader.getElementText()));
                    } else if (ResultPacket.Schema.COOLER_WEIGHTINGS.equals(xmlStreamReader.getLocalName())) {
                        while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
                            CoolValue cv = parseCoolValue(xmlStreamReader);
                            packet.getCoolerWeights().put(new CoolerWeighting(cv.name, cv.id), Float.parseFloat(cv.value));
                        }
                    } else if (ResultPacket.Schema.COOLER_NAMES.equals(xmlStreamReader.getLocalName())) {
                        while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
                            CoolValue cv = parseCoolValue(xmlStreamReader);
                            packet.getCoolerNames().put(new CoolerWeighting(cv.name, cv.id), cv.value);
                        }
                    } else if (ResultPacket.Schema.STOP_WORDS.equals(xmlStreamReader.getLocalName())) {
                        String[] stopWords = xmlStreamReader.getElementText().split("\\s+");
                        for(String word : stopWords){
                            if(!"".equals(word)) packet.getStopWords().add(word);
                        }
                    } else if (ResultPacket.Schema.STEM_EQUIV.equals(xmlStreamReader.getLocalName())) {
                        String[] A = xmlStreamReader.getElementText().split("\n"); 
                        for(String line : A) {
                            line = line.trim();
                            if("".equals(line)) continue;
                            String[] originalAndTarget = line.split(":");
                            if(originalAndTarget[1].indexOf("[") == -1) {
                                packet.getStemmedEquivs().put(originalAndTarget[1], originalAndTarget[0]);
                            } else {
                                originalAndTarget[1] = originalAndTarget[1].replaceAll("[\\[\\]]", "");
                                String[] targets =originalAndTarget[1].split("\\s+");
                                for(String target : targets) {
                                    packet.getStemmedEquivs().put(target, originalAndTarget[0]);
                                }
                            }
                        }
                        
                    } else if (ResultPacket.Schema.EXPLAIN_TYPES.equals(xmlStreamReader.getLocalName())) {
                        while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
                            CoolValue cv = parseCoolValue(xmlStreamReader);
                            packet.getExplainTypes().put(new CoolerWeighting(cv.name, cv.id), cv.value);
                        }
                    } else if (ResultPacket.Schema.SVGS.equals(xmlStreamReader.getLocalName())) {
                        packet.getSvgs().putAll(XmlStreamUtils.tagsToMap(ResultPacket.Schema.SVGS, xmlStreamReader));
                    } else {
                        log.warn("Unkown tag '" + xmlStreamReader.getLocalName() + "' at root level");
                    }
                    
                    break;
                }
            }

            xmlStreamReader.close();
            return packet;
        } catch (XMLStreamException ioe) {
            throw new XmlParsingException(ioe);
        }
        
    }

    private void parseResults(XMLStreamReader xmlStreamReader, ResultPacket packet) throws XMLStreamException {
        
        
        int type = xmlStreamReader.getEventType();
        TierBar lastTierBar = null;
        int tierBarFirstRank = 0;
        int tierBarLastRank = 0;
        do {
            type = xmlStreamReader.next();
            
            switch(type) {
            case XMLStreamReader.START_ELEMENT:
                if (Result.Schema.RESULT.equals(xmlStreamReader.getLocalName())) {
                    packet.getResults().add(ResultFactory.fromXmlStreamReader(xmlStreamReader));
                    tierBarLastRank++;
                } else if (TierBar.Schema.TIER_BAR.equals(xmlStreamReader.getLocalName())) {
                    if ( lastTierBar != null) {
                        // There was a tier bar before
                        lastTierBar.setLastRank(tierBarLastRank);
                        packet.getTierBars().add(lastTierBar);
                        tierBarFirstRank = tierBarLastRank;
                    }
                    lastTierBar = TierBarFactory.fromXmlStreamReader(xmlStreamReader);
                    lastTierBar.setFirstRank(tierBarFirstRank);
                } else {
                    log.warn("Unkown tag '" + xmlStreamReader.getLocalName() + "' in the " + ResultPacket.Schema.RESULTS + " block");
                }
                break;
            }
        } while( type != XMLStreamReader.END_ELEMENT || ( type == XMLStreamReader.END_ELEMENT && !ResultPacket.Schema.RESULTS.equals(xmlStreamReader.getLocalName())) );
        
        if ( lastTierBar != null) {
            // There was a last tier bar before
            lastTierBar.setLastRank(tierBarLastRank);
            packet.getTierBars().add(lastTierBar);
        }
    }
    
    /**
     * Parses a single <rmc> tag
     * @param xmlStreamReader
     * @return
     * @throws NumberFormatException
     * @throws XMLStreamException
     */
    private RMC parseRmc(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
        if(!ResultPacket.Schema.RMC.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        if ( xmlStreamReader.getAttributeCount() == 1
            && ResultPacket.Schema.RMC_ITEM.equals(xmlStreamReader.getAttributeLocalName(0))) {
            RMC rmc = new RMC();
            rmc.item = xmlStreamReader.getAttributeValue(0);
            rmc.count = Integer.parseInt(xmlStreamReader.getElementText());
            return rmc; 
        }
        
        return null;
    }

    /**
     * Parses a single <md_range> tag
     * @param xmlStreamReader
     * @return
     * @throws NumberFormatException
     * @throws XMLStreamException
     */
    private MetadataRange parseMetadataRange(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
        if(!ResultPacket.Schema.METADATA_RANGE.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        if ( xmlStreamReader.getAttributeCount() == 3
            && ResultPacket.Schema.METADATA_RANGE_CLASS.equals(xmlStreamReader.getAttributeLocalName(0))
            && ResultPacket.Schema.METADATA_RANGE_MIN.equals(xmlStreamReader.getAttributeLocalName(1))
            && ResultPacket.Schema.METADATA_RANGE_MAX.equals(xmlStreamReader.getAttributeLocalName(2))) {
            MetadataRange metadataRange = new MetadataRange();
            metadataRange.field = xmlStreamReader.getAttributeValue(0);
            metadataRange.min = Double.parseDouble(xmlStreamReader.getAttributeValue(1));
            metadataRange.max = Double.parseDouble(xmlStreamReader.getAttributeValue(2));
            return metadataRange; 
        }
        
        return null;
    }

    public static CoolValue parseCoolValue(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if (xmlStreamReader.getAttributeCount() == 1
                && ResultPacket.Schema.COOL.equals(xmlStreamReader.getAttributeLocalName(0))) {
            CoolValue cv = new CoolValue();
            cv.id = Integer.parseInt(xmlStreamReader.getAttributeValue(0));
            cv.name = xmlStreamReader.getLocalName();
            cv.value = xmlStreamReader.getElementText();
            
            return cv;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Parses an RMC from a &lt;rmc_item_results&gt; tag
     * @param xmlStreamReader
     * @return
     */
    private RMC parseRmcFromRmcItemResults(XMLStreamReader xmlStreamReader) {
        if (!ResultPacket.Schema.RMC_ITEM_RESULTS.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        if ( xmlStreamReader.getAttributeCount() == 2
                && ResultPacket.Schema.RMC_ITEM.equals(xmlStreamReader.getAttributeLocalName(0))
            && ResultPacket.Schema.RMC_COUNT.equals(xmlStreamReader.getAttributeLocalName(1))) {
                RMC rmc = new RMC();
                rmc.item = xmlStreamReader.getAttributeValue(0);
                rmc.count = Integer.parseInt(xmlStreamReader.getAttributeValue(1));
                return rmc; 
            }
            
            return null;
    }
    
    private List<RMCItemResult> parseRmcItemResults(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        List<RMCItemResult> out = new ArrayList<RMCItemResult>();
        
        int type = xmlStreamReader.getEventType();
        do {
            type = xmlStreamReader.next();
            
            if (type == XMLStreamReader.START_ELEMENT
                && ResultPacket.Schema.RMC_ITEM_RESULT.equals(xmlStreamReader.getLocalName())) {
                out.add(RMCItemResultFactory.fromXmlStreamReader(xmlStreamReader));
            }
        } while( type != XMLStreamReader.END_ELEMENT || !ResultPacket.Schema.RMC_ITEM_RESULTS.equals(xmlStreamReader.getLocalName()) );
        
        return out;    
    }
    
    private QSup parseQSup(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if (!ResultPacket.Schema.QSUP.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        if ( xmlStreamReader.getAttributeCount() == 1
                && ResultPacket.Schema.QSUP_SRC.equals(xmlStreamReader.getAttributeLocalName(0))) {
            try {
                return new QSup(
                        Source.valueOf(xmlStreamReader.getAttributeValue(0)),
                        xmlStreamReader.getElementText()
                        );
            } catch (Exception e) {
                return new QSup(Source.Unknown, xmlStreamReader.getElementText());
            }
        }
        return null;
    }
    
    private Map<String, Integer> parseEntityList(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        if (!ResultPacket.Schema.ENTITYLIST.equals(xmlStreamReader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        Map<String, Integer> out = new HashMap<String, Integer>();
        
        int type;
        do {
            type = xmlStreamReader.nextTag();
            
            if ( type == XMLStreamReader.START_ELEMENT && ResultPacket.Schema.ENTITY.equals(xmlStreamReader.getLocalName())
                    && xmlStreamReader.getAttributeCount() == 1
                    && ResultPacket.Schema.CNT.equals(xmlStreamReader.getAttributeLocalName(0))) {
                int cnt = Integer.parseInt(xmlStreamReader.getAttributeValue(0));
                out.put(xmlStreamReader.getElementText(), cnt);
            }
            
        } while(type != XMLStreamReader.END_ELEMENT || ( type == XMLStreamReader.END_ELEMENT && !ResultPacket.Schema.ENTITYLIST.equals(xmlStreamReader.getLocalName())));
        
        return out;
        
    }
    
    private Float[] parseOrigin(String originString) {
        String[] origin = originString.split(",");
        
        if (origin.length != 2) {
            throw new IllegalArgumentException("Invalid origin string: '" + originString +"'. It should be two floats separated by a comma");
        }
        
        Float[] out = new Float[2];
        out[0] = Float.parseFloat(origin[0]);
        out[1] = Float.parseFloat(origin[1]);    
        
        return out;
    }
    
    /**
     * Parses a single &lt;urlcount&gt; tag
     * @param reader
     * @return
     * @throws NumberFormatException
     * @throws XMLStreamException
     */
    private URLCount parseURLCount(XMLStreamReader reader) throws NumberFormatException, XMLStreamException {
        if (!ResultPacket.Schema.URLCOUNT.equals(reader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        if ( reader.getAttributeCount() == 1
                && ResultPacket.Schema.URLCOUNT_ITEM.equals(reader.getAttributeLocalName(0))) {
                URLCount url = new URLCount();
                url.url = reader.getAttributeValue(0);
                url.count = Integer.parseInt(reader.getElementText());
                return url; 
            }
            
            return null;
    }

    /**
     * Parses a single &lt;datecount&gt; tag
     * @param reader
     * @return
     * @throws NumberFormatException
     * @throws XMLStreamException
     */
    private DateCount parseDateCount(XMLStreamReader reader) throws NumberFormatException, XMLStreamException {
        if (!ResultPacket.Schema.DATECOUNT.equals(reader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        if (reader.getAttributeCount() == 2
                && ResultPacket.Schema.DATECOUNT_ITEM.equals(reader.getAttributeLocalName(0))
                && ResultPacket.Schema.DATECOUNT_QTERM.equals(reader.getAttributeLocalName(1))) {
            DateCount dc = new DateCount();
            dc.item = reader.getAttributeValue(0);
            dc.qTerm = reader.getAttributeValue(1);
            dc.count = Integer.parseInt(reader.getElementText());
            return dc;
        }
        
        return null;
    }
    
    /**
     * Parses <gscope_counts> tag and its childrens
     * @param reader
     * @return
     * @throws XMLStreamException 
     * @throws NumberFormatException 
     */
    private Map<Integer, Integer> parseGScopeCounts(XMLStreamReader reader) throws NumberFormatException, XMLStreamException {
        if (!ResultPacket.Schema.GSCOPE_COUNTS.equals(reader.getLocalName())) {
            throw new IllegalArgumentException();
        }
        
        HashMap<Integer, Integer> out = new HashMap<Integer, Integer>();

        while(reader.hasNext()&&
                ! (reader.next() == XMLStreamReader.END_ELEMENT && ResultPacket.Schema.GSCOPE_COUNTS.equals(reader.getLocalName()))) {
            
            if (reader.getEventType() == XMLStreamReader.START_ELEMENT
                    && ResultPacket.Schema.GSCOPE_MATCHING.equals(reader.getLocalName())) {
                int gScopeValue = Integer.parseInt(reader.getAttributeValue(null, ResultPacket.Schema.GSCOPE_VALUE));
                int count = Integer.parseInt(reader.getElementText());
                out.put(gScopeValue, count);
            }
        }
        
        return out;
    }
    
    /**
     * Parses &lt;phluster_elapsed_time&gt;. It's a floating number
     * followed by a space and "sec.". Ex: '0.020 sec.'
     * @param data
     * @return
     */
    private Float parsePhlusterElapsedTime(String data) {
        if (data == null || "".equals(data)) {
            return null;
        } else {
            return Float.parseFloat(data.substring(0, data.indexOf(" ")));
        }
    }

    /**
     * Parses a include/exclude scope string (separated by @)
     * @param scopeString
     * @return
     */
    private List<String> parseScopes(String scopeString) {
        if (scopeString == null || "".equals(scopeString)) {
            return new ArrayList<String>();
        } else {
            return Arrays.asList(scopeString.split(ResultPacket.Schema.SCOPE_SEPARATOR));
        }
    }
    
    private class RMC {
        public String item;
        public int count;
    }

    private class MetadataRange {
        public String field;
        public double min;
        public double max;
    }

    public static class CoolValue {
        public String name;
        public int id;
        public String value;
    }
    
    private class URLCount {
        public String url;
        public int count;
    }
    
    private class DateCount {
        public String item;
        public String qTerm;
        public int count;
    }
    
}
