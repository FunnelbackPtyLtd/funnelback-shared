package com.funnelback.publicui.search.model.padre.factories;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.XmlStreamUtils;
import com.funnelback.publicui.search.model.padre.Result;

/**
 * Builds {@link Result}s from various input sources.
 * 
 */
public class ResultFactory {

	/**
	 * Builds a {@link Result} from a Map containing the values
	 * 
	 * @param data
	 *            The map containing the values
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
		Date date;
		try {
			date = Result.DATE_FORMAT.parse(dateString);
		} catch (Exception e) {
			date = null;
		}

		Integer fileSize = Integer.valueOf(data.get(Result.Schema.FILESIZE));
		String fileType = data.get(Result.Schema.FILETYPE);
		Integer tier = Integer.valueOf(data.get(Result.Schema.TIER));
		Integer documentNumber = Integer.valueOf(data.get(Result.Schema.DOCNUM));

		HashMap<String, String> metadataMap = new HashMap<String, String>();
		for (String key : data.keySet()) {
			if (key.startsWith(Result.METADATA_PREFIX)) {
				metadataMap.put(key, data.get(key));
			}
		}

		return new Result(
				rank,
				score,
				title,
				collection,
				component,
				liveUrl,
				summary,
				cacheUrl,
				date,
				fileSize,
				fileType,
				tier,
				documentNumber,
				metadataMap,
				liveUrl);
	}

	/**
	 * Builds a {@link Result} from an {@link XMLEventReader}. Doesn't supports
	 * MetaData reading.
	 * 
	 * @param xmlEventReader
	 *            The {@link XMLEventReader} to read from
	 * @return A Result with populated values
	 * @throws XMLStreamException
	 */
	public static Result fromXmlEventReader(XMLEventReader xmlEventReader) throws XMLStreamException {
		XMLEvent outerEvent;
		while (xmlEventReader.hasNext()) {
			outerEvent = xmlEventReader.nextEvent();
			if (outerEvent.isStartElement()
					&& outerEvent.asStartElement().getName().getLocalPart().endsWith(Result.Schema.RESULT)) {
				// We found our result - read out each bit until the end

				Map<String, String> data = new HashMap<String, String>();
				XMLEvent innerEvent;
				while ((innerEvent = xmlEventReader.nextEvent()) != null) {
					if (innerEvent.isStartElement()) {
						String key = innerEvent.asStartElement().getName().getLocalPart();
						String value = xmlEventReader.getElementText();

						data.put(key, value);
					} else if (innerEvent.isEndElement()) {
						return fromMap(data);
					}
				}
			}
		}

		// No result found
		return null;
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
		return fromMap(XmlStreamUtils.tagsToMap(Result.Schema.RESULT, xmlStreamReader));
	}
}
