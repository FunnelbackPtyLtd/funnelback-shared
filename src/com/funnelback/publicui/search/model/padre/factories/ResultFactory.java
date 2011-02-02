package com.funnelback.publicui.search.model.padre.factories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.funnelback.publicui.search.model.padre.Result;

/**
 * Builds {@link Result}s from various input sources.
 * 
 */
public class ResultFactory {

	private static final Map<Long, SimpleDateFormat> dateFormatters = new HashMap<Long, SimpleDateFormat>();
	
	/**
	 * Builds a {@link Result} from a Map containing the values
	 * 
	 * @param data
	 *            The map containing the values
	 * @param metadata
	 *            A map containing the metadata values (<md f="x">value</md>)
	 * @return A result with populated values
	 */
	public static Result fromMap(Map<String, String> data) {
		Integer rank = Integer.valueOf(data.get(Result.Schema.RANK));
		Integer score = Integer.valueOf(data.get(Result.Schema.SCORE));
		String title = data.get(Result.Schema.TITLE);
		String collection = data.get(Result.Schema.COLLECTION);
		Integer component = Integer.valueOf(data.get(Result.Schema.COMPONENT));
		String liveUrl = data.get(Result.Schema.LIVE_URL);
		String summary = data.get(Result.Schema.SUMMARY);
		String cacheUrl = data.get(Result.Schema.CACHE_URL);

		String dateString = data.get(Result.Schema.DATE);
		Date date = null;
		if (dateString != null && !"".equals(dateString)) {
			try {
				date = getDateFormatter().parse(dateString);
			} catch (Exception e) {
			}
		}

		Integer fileSize = Integer.valueOf(data.get(Result.Schema.FILESIZE));
		String fileType = data.get(Result.Schema.FILETYPE);
		Integer tier = Integer.valueOf(data.get(Result.Schema.TIER));
		Integer documentNumber = Integer.valueOf(data.get(Result.Schema.DOCNUM));

		HashMap<String, String> metadataMap = new HashMap<String, String>();
		for (String key : data.keySet()) {
			if (key.startsWith(Result.METADATA_PREFIX)) {
				metadataMap.put(key.substring(Result.METADATA_PREFIX.length()), data.get(key));
			}
		}

		return new Result(rank, score, title, collection, component, liveUrl, summary, cacheUrl, date, fileSize,
				fileType, tier, documentNumber, metadataMap, liveUrl);
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

		while (xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
			if (xmlStreamReader.isStartElement()) {
				if (Result.Schema.METADATA.equals(xmlStreamReader.getLocalName().toString())) {
					// Specific case for metadtata <md f="x">value</md>
					String mdClass = xmlStreamReader.getAttributeValue(null, Result.Schema.ATTR_METADATA_F);
					String value = xmlStreamReader.getElementText();
					data.put(Result.METADATA_PREFIX + mdClass, value);
				} else {
					String name = xmlStreamReader.getName().toString();
					String value = xmlStreamReader.getElementText();
					data.put(name, value);
				}
			}
		}

		return fromMap(data);
	}
	
	private static SimpleDateFormat getDateFormatter() {
		SimpleDateFormat df = dateFormatters.get(Thread.currentThread().getId());
		if (df == null) {
			df = new SimpleDateFormat(Result.DATE_PATTERN);
			dateFormatters.put(Thread.currentThread().getId(), df);
		}
		return df;
	}
}
