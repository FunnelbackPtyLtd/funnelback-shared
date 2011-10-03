package com.funnelback.publicui.search.service.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Type;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;

/**
 * Implementation that assumes that the indexes are on the local disk.
 */
@CommonsLog
public abstract class AbstractLocalIndexRepository implements IndexRepository {

	@Autowired
	@Setter private ConfigRepository configRepository;
	
	/**
	 * Reads the index_time file to get the last update time
	 * of the index.
	 * @param collectionId
	 * @return The updated Date, or null if the date is not available.
	 * @throws UnsupportedOperationException If the collection doesn't have a
	 * <code>index_time</code> parameter (such as meta collections)
	 */
	protected Date loadLastUpdated(String collectionId) throws UnsupportedOperationException {
		Collection c = configRepository.getCollection(collectionId);
		if (c == null) {
			return null;
		}
		if (Type.meta.equals(c.getType())) {
			throw new UnsupportedOperationException("Meta collection don't have an updated time");
		}
		
		try {
			File indexTimeFile = getIndexFile(collectionId, Files.Index.INDEX_TIME);
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
	
	/**
	 * Loads the <code>.bldinfo</code> file of a collection and returns its
	 * content as a Map.
	 * @param collectionId
	 * @return
	 * @throws UnsupportedOperationException If the collection doesn't have a
	 * <code>.bldinfo</code> parameter (such as meta collections)
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String> loadBuildInfo(String collectionId) throws UnsupportedOperationException{
		Collection c = configRepository.getCollection(collectionId);
		if (c == null) {
			return null;
		}
		if (Type.meta.equals(c.getType())) {
			throw new UnsupportedOperationException("Meta collection don't have a build info file");
		}
		
		try {
			File indexBldInfoFile = getIndexFile(collectionId, Files.Index.BLDINFO);
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
				out.put(BuildInfoKeys.indexer_arguments.toString(), StringUtils.join(indexerOptions, "\n"));
				return out;
			}
		} catch (IOException ioe) {
			log.error("Could not load bldinfo file for collection '" + collectionId + "'", ioe);
		}
		
		return null;
	}
	
	protected File getIndexFile(String collectionId, String fileName) {
		try {
			return new File(configRepository.getCollection(collectionId).getConfiguration().getCollectionRoot()
					+ File.separator + DefaultValues.VIEW_LIVE
					+ File.separator + DefaultValues.FOLDER_IDX,
					fileName);
		} catch (FileNotFoundException fnfe) {
			log.error("Error while accessing index file '"+fileName+"' for collection '"+collectionId+"'", fnfe);
			return null;
		}
	}


}
