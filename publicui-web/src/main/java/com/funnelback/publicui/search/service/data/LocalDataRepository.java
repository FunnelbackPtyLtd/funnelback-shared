package com.funnelback.publicui.search.service.data;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Repository;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.DataRepository;

@Repository
@Log4j
public class LocalDataRepository implements DataRepository {

	private static final String[] FOLDERS = {
		DefaultValues.VIEW_LIVE + File.separator + DefaultValues.VIEW_FOLDER_DATA,
		DefaultValues.VIEW_LIVE + File.separator + DefaultValues.VIEW_FOLDER_SECONDARY_DATA,
		DefaultValues.VIEW_OFFLINE + File.separator + DefaultValues.VIEW_FOLDER_DATA
	};
	
	@Override
	public String getCachedDocument(Collection collection, String relativeUrl) {
		File doc = null;
		for (String folder: FOLDERS) {
			doc = new File(collection.getConfiguration().getCollectionRoot()
				+ File.separator + folder, relativeUrl);
			if (doc.exists()) {
				break;
			}
			log.debug("Cached document '" + doc.getAbsolutePath() + "' doesn't exist.");
		}
		if (doc.exists()) {
			try {
				return FileUtils.readFileToString(doc);
			} catch (IOException ioe) {
				// Fail silently for the user
				log.error("Unable to read cached document at '" + doc.getAbsolutePath() + "'", ioe);
				return null;
			}
		} else {
			return null;
		}
	}
}
