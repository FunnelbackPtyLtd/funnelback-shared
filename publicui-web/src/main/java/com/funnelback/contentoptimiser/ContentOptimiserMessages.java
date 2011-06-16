package com.funnelback.contentoptimiser;

public class ContentOptimiserMessages {

	
	public static final String SELECTED_DOCUMENT_TOO_FAR_DOWN = "The selected document appeared too far down the ranking to be examined.";
	
	
	// Errors for cache file generation. These probably won't happen too often.
	public static final String ERROR_CREATING_CACHE_FILE = "There was an error creating the file to store the cached copy of the document. The content score breakdown will be unavailable.";
	public static final String ERROR_CALLING_CACHE_CGI = "Unable to obtain selected document from cache. The content score breakdown will be unavailable.";
	public static final String ERROR_CONFIG_FILE_NOT_FOUND = "Unable to open the collection configuration file. The content score breakdown will be unavailable";
	public static final String ERROR_CALLING_INDEXER = "Unable to index the selected document from cache. The content score breakdown will be unavailable.";
	public static final String ERROR_READING_INDEXED_FILE = "Unable to read the words seen by the indexer for the selected document. The content score breakdown will be unavailable.";
	public static final String ERROR_READING_BLDINFO = "Unable to read the previous indexer options from the bldinfo file. The content score breakdown will be unavailable.";

}
