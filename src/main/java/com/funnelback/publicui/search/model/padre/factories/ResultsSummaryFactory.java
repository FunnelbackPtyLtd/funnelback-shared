package com.funnelback.publicui.search.model.padre.factories;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.xml.XmlStreamUtils;
import com.funnelback.publicui.xml.XmlStreamUtils.TagAndText;
import com.google.common.collect.ImmutableMap;

public class ResultsSummaryFactory {

    
    public static class ResultSummaryFiller {
        private final Map<String, Consumer<String>> ON_TAG;
        
        public ResultSummaryFiller(ResultsSummary rs) {
             ON_TAG = ImmutableMap.<String, Consumer<String>>builder()
                .put(ResultsSummary.Schema.FULLY_MATCHING, asInt(rs::setFullyMatching))
                .put(ResultsSummary.Schema.COLLAPSED, asInt(rs::setCollapsed))
                .put(ResultsSummary.Schema.PARTIALLY_MATCHING, asInt(rs::setPartiallyMatching))
                .put(ResultsSummary.Schema.TOTAL_MATCHING, asInt(rs::setTotalMatching))
                .put(ResultsSummary.Schema.CARRIED_OVER_FTD, asInt(rs::setCarriedOverFtd))
                .put(ResultsSummary.Schema.TOTAL_DISTINCT_MATCHING_URLS, asInt(rs::setTotalDistinctMatchingUrls))
                .put(ResultsSummary.Schema.NUM_RANKS, asInt(rs::setNumRanks))
                .put(ResultsSummary.Schema.CURRSTART, asInt(rs::setCurrStart))
                .put(ResultsSummary.Schema.CURREND, asInt(rs::setCurrEnd))
                .put(ResultsSummary.Schema.PREVSTART, asInt(rs::setPrevStart))
                .put(ResultsSummary.Schema.NEXTSTART, asInt(rs::setNextStart))
                .put(ResultsSummary.Schema.TOTAL_SECURITY_OBSCURED_URLS, asInt(rs::setTotalSecurityObscuredUrls))
                .put(ResultsSummary.Schema.ESTIMATED_COUNTS, asBool(rs::setEstimatedCounts))
                .put(ResultsSummary.Schema.ANY_URLS_PROMOTED, asBool(rs::setAnyUrlsPromoted))
                .build();
        }
        
        public void onTag(String tagName, String data) {
            Optional.ofNullable(ON_TAG.get(tagName)).ifPresent(c -> c.accept(data));
        }
        
        private static Consumer<String> asInt(Consumer<Integer> c) {
            return (data) -> c.accept(Integer.parseInt(data));
        }
        
        private static Consumer<String> asBool(Consumer<Boolean> c) {
            return (data) -> c.accept(Boolean.parseBoolean(data));
        }
    }
    
    /**
     * Fills in a {@link ResultsSummary} from a {@link XMLStreamReader}.
     * Assumes that the stream is currently inside the <results_summary> tag.
     * @param xmlStreamReader
     * @return
     * @throws NumberFormatException
     * @throws XMLStreamException
     */
    public static void fromXmlStreamReader(XMLStreamReader xmlStreamReader, ResultsSummary resultsSummary) throws NumberFormatException, XMLStreamException {
        if( ! ResultsSummary.Schema.RESULTS_SUMMARY.equals(xmlStreamReader.getLocalName()) ) {
            throw new IllegalArgumentException();
        }
        
        ResultSummaryFiller filler = new ResultSummaryFiller(resultsSummary);
        
        while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
            if (xmlStreamReader.isStartElement()) {
                // Start tag for an result entry
                TagAndText tt = XmlStreamUtils.getTagAndValue(xmlStreamReader);
                filler.onTag(tt.tag, tt.text);
            }
        }
    }
    
}
