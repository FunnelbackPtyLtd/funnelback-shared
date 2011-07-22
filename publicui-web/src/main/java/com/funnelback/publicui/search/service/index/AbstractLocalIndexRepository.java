package com.funnelback.publicui.search.service.index;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;

/**
 * Implementation that assumes that the indexes are on the local disk.
 */
@Log
public abstract class AbstractLocalIndexRepository implements IndexRepository {

	@Autowired
	@Setter private ConfigRepository configRepository;
	
	/**
	 * Reads the index_time file to get the last update time
	 * of the index.
	 * @param collectionId
	 * @return The updated Date, or null if the date is not available.
	 */
	protected Date loadLastUpdated(String collectionId) {
		Collection c = configRepository.getCollection(collectionId);
		if (c == null) {
			return null;
		}
		
		try {
			File indexTimeFile = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX,
					Files.Index.INDEX_TIME);
			if (indexTimeFile.canRead()) {
				String time = FileUtils.readFileToString(indexTimeFile);
				try {
					return new SimpleDateFormat(Details.UPDATED_DATE_PATTERN).parse(time);
				} catch (Exception e) {
					log.warn("Could not parse last update date string '" + time + "' from '" + indexTimeFile.getAbsolutePath() + "'.", e);
				}
			}
		} catch (IOException ioe) {
			log.error("Could not load last updated date for collection '" + collectionId + "'", ioe);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, String> loadBuildInfo(String collectionId) {
		Collection c = configRepository.getCollection(collectionId);
		if (c == null) {
			return null;
		}
		
		try {
			File indexBldInfoFile = new File(c.getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX,
					Files.Index.BLDINFO);
			if (indexBldInfoFile.canRead()) {
				Map<String, String> out = new HashMap<String, String>();
				List<String> indexerOptions = new ArrayList<String>();
				for (String line: (List<String>) FileUtils.readLines(indexBldInfoFile)) {
					if (line.startsWith(BuildInfoKeys.version.toString())) {
						out.put(BuildInfoKeys.version.toString(), line);
					} else if (line.matches("^.+?:\\s.+$")) {
						// Key: value type
						out.put(
							line.substring(0, line.indexOf(":")),
							line.substring(line.indexOf(": ")+2));						
					} else if (line.matches("^[A-Z_-]+$")) {
						// Single uppercase word, it's a flag
						out.put(line, null);
					} else {
						// None of the above matched, it's probably
						// an indexer option from the top of the file
						indexerOptions.add(line);
					}
				}
				out.put(BuildInfoKeys.indexer_options.toString(), StringUtils.join(indexerOptions, "\n"));
				return out;
			}
		} catch (IOException ioe) {
			log.error("Could not load bldinfo file for collection '" + collectionId + "'", ioe);
		}
		
		return null;
	}


}
