package com.funnelback.publicui.search.model.padre.factories;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.ResultsSummary;

public class ResultsSummaryFactory {

	public static ResultsSummary fromMap(Map<String, Integer> data) {
		return new ResultsSummary(
				data.get(ResultsSummary.Schema.FULLY_MATCHING),
				data.get(ResultsSummary.Schema.ESTIMATED_HITS),
				data.get(ResultsSummary.Schema.PARTIALLY_MATCHING),
				data.get(ResultsSummary.Schema.TOTAL_MATCHING),
				data.get(ResultsSummary.Schema.NUM_RANKS),
				data.get(ResultsSummary.Schema.CURRSTART),
				data.get(ResultsSummary.Schema.CURREND),
				data.get(ResultsSummary.Schema.PREVSTART),
				data.get(ResultsSummary.Schema.NEXTSTART));
	}
	
	/**
	 * Builds a {@link ResultsSummary} from a {@link XMLStreamReader}.
	 * Assumes that the stream is currently inside the <results_summary> tag.
	 * @param xmlStreamReader
	 * @return
	 * @throws NumberFormatException
	 * @throws XMLStreamException
	 */
	public static ResultsSummary fromXmlStreamReader(XMLStreamReader xmlStreamReader) throws NumberFormatException, XMLStreamException {
		if( ! ResultsSummary.Schema.RESULTS_SUMMARY.equals(xmlStreamReader.getLocalName()) ) {
			throw new IllegalArgumentException();
		}
		
		Map<String, Integer> data = new HashMap<String, Integer>();

		while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
			if (xmlStreamReader.isStartElement()) {
				// Start tag for an result entry
				String name = xmlStreamReader.getName().toString();
				String value = xmlStreamReader.getElementText();
				data.put(name, Integer.parseInt(value));
			}		
		}
		
		return fromMap(data);
	}
	
}
