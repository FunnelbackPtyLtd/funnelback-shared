package com.funnelback.publicui.recommender;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.utils.FileUtils;
import com.funnelback.publicui.recommender.tuple.ItemTuple;
import com.funnelback.publicui.recommender.tuple.PreferenceTuple;
import com.funnelback.publicui.recommender.utils.DataModelUtils;
import com.funnelback.publicui.recommender.utils.ItemUtils;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;
import lombok.Data;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.MemoryIDMigrator;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a <a href="http://en.wikipedia.org/wiki/Recommender_system">Recommender System</a>.
 * It is a singleton that is loaded in once by a web application server and will provide recommendation
 * methods to callers.
 *
 * It currently acts as a wrapper around the Apache Mahout recommender library:
 *
 *     <a href="http://mahout.apache.org/">http://mahout.apache.org/</a>
 *
 * @author fcrimmins@funnelback.com
 */
@Data
public class FBRecommender {
    private static FBRecommender instance = null;
    private static ItemBasedRecommender recommender = null;
    private static DataModel dataModel;
    private MemoryIDMigrator memoryIDMigrator = new MemoryIDMigrator();
    private Map<Long,List<Preference>> userIDPreferencesMap = new HashMap<>();
    private Map<String, Set<String>> itemUserMap = new HashMap<>();
    private Map<String, List<PreferenceTuple>> userSessionsMap = new HashMap<>();
    private Map<Long, String> IDStringMap = new HashMap<>();
    private Map<String, MutableLong> itemFrequencyMap = new HashMap<>();
    private static final int MAX_MODEL_RECOMMENDATIONS = 100;
    private Logger log = Logger.getLogger(FBRecommender.class.getName());
    public static final long TIME_WINDOW_SIZE = 300000;   // 5 minutes
    public static final String DATA_MODEL_CHECKPOINT_FILE = "recommender.model";
    public static final String PREFERENCES_CHECKPOINT_FILE = "recommender.user_id_preferences_map";
    public static final String ITEM_USER_MAP_CHECKPOINT_FILE = "recommender.item_user_map";
    public static final String SESSIONS_MAP_CHECKPOINT_FILE = "recommender.user_sessions_map";
    public static final String ID_STRING_CHECKPOINT_FILE = "recommender.id_string_map";
    public static final String ITEM_FREQUENCY_CHECKPOINT_FILE = "recommender.item_frequency_map";
    private static final String INITIAL_COLLECTION = "test-curtin-courses";

    // Private constructor so callers cannot instantiate class and must use getInstance() instead
    private FBRecommender() {
    }

    /**
     * Return a single instance of a FBRecommender to all callers.
     * @return FBRecommender instance.
     */
    public synchronized static FBRecommender getInstance() {
    	
        if (instance == null) {
            instance = new FBRecommender();
            instance.initRecommender(INITIAL_COLLECTION);
        }
        return instance;
    }

    /**
     * Initialize key data structures in case we were unable to restore them from checkpoint.
     */
    private void initializeDataStructures() {
        memoryIDMigrator = new MemoryIDMigrator();
        userIDPreferencesMap = new HashMap<>();
        itemUserMap = new HashMap<>();
        userSessionsMap = new HashMap<>();
        IDStringMap = new HashMap<>();
        itemFrequencyMap = new HashMap<>();
    }

    /**
     * Restore data structures for the given collection.
     * @param collectionConfig Configuration details for the collection
     * @return true if all relevant data structures were correctly restored
     */
    private boolean restoreCollectionStructures(Config collectionConfig) {
        long startTime = System.currentTimeMillis();

        boolean restored = false;
        File checkpointDir = new File(collectionConfig.getCollectionRoot() + File.separator + "live" + File.separator
                + "checkpoint");

        File checkpointFile = new File(checkpointDir + File.separator + DATA_MODEL_CHECKPOINT_FILE);

        if (checkpointFile.exists()) {
            System.out.println("Loading checkpointed data structures from file ...");
            dataModel = (DataModel) FileUtils.loadObject(checkpointFile);
            checkpointFile = new File(checkpointDir + File.separator + PREFERENCES_CHECKPOINT_FILE);
            userIDPreferencesMap = (Map<Long,List<Preference>>) FileUtils.loadObject(checkpointFile);
            checkpointFile = new File(checkpointDir + File.separator + ITEM_USER_MAP_CHECKPOINT_FILE);
            itemUserMap = (Map<String, Set<String>>) FileUtils.loadObject(checkpointFile);
            checkpointFile = new File(checkpointDir + File.separator + SESSIONS_MAP_CHECKPOINT_FILE);
            userSessionsMap = (Map<String, List<PreferenceTuple>>) FileUtils.loadObject(checkpointFile);
            checkpointFile = new File(checkpointDir + File.separator + ID_STRING_CHECKPOINT_FILE);
            IDStringMap = (Map<Long, String>) FileUtils.loadObject(checkpointFile);
            checkpointFile = new File(checkpointDir + File.separator + ITEM_FREQUENCY_CHECKPOINT_FILE);
            itemFrequencyMap = (Map<String, MutableLong>) FileUtils.loadObject(checkpointFile);
        }

        if (dataModel != null && userIDPreferencesMap != null && itemUserMap != null
                && userSessionsMap != null && IDStringMap != null) {
            long timeTaken = (System.currentTimeMillis() - startTime);
            System.out.println("Restored checkpointed data structures from file - took " + timeTaken + "ms");
            restored = true;
        }
        else {
            System.out.println("Problem restoring checkpointed data structures from file - will have " +
                    "to regenerate all data structures from scratch.");
            initializeDataStructures();
        }

        return restored;
    }

    /**
     * Checkpoint the given collection to disk, to allow it to be restored at some later point in time.
     * @param collectionConfig Config object for given collection.
     */
    private void checkpointCollection(Config collectionConfig) {
        File checkpointDir = new File(collectionConfig.getCollectionRoot() + File.separator + "live" + File.separator
                + "checkpoint");

        File checkpointFile = new File(checkpointDir + File.separator + DATA_MODEL_CHECKPOINT_FILE);
        RecommenderUtils.writeCheckpoint(checkpointFile, dataModel);
        checkpointFile = new File(checkpointDir + File.separator + PREFERENCES_CHECKPOINT_FILE);
        RecommenderUtils.writeCheckpoint(checkpointFile, userIDPreferencesMap);
        checkpointFile = new File(checkpointDir + File.separator + ITEM_USER_MAP_CHECKPOINT_FILE);
        RecommenderUtils.writeCheckpoint(checkpointFile, itemUserMap);
        checkpointFile = new File(checkpointDir + File.separator + SESSIONS_MAP_CHECKPOINT_FILE);
        RecommenderUtils.writeCheckpoint(checkpointFile, userSessionsMap);
        checkpointFile = new File(checkpointDir + File.separator + ID_STRING_CHECKPOINT_FILE);
        RecommenderUtils.writeCheckpoint(checkpointFile, IDStringMap);
        checkpointFile = new File(checkpointDir + File.separator + ITEM_FREQUENCY_CHECKPOINT_FILE);
        RecommenderUtils.writeCheckpoint(checkpointFile, itemFrequencyMap);
    }

    /**
     * Initialize the recommender (if it is not null). If an existing serialized data model is available
     * on disk then this will be used as input to the recommender. Otherwise the model will be built from
     * scratch by loading in data from a CSV file.
     */
	private void initRecommender(String collection) {
        if (recommender == null) {
            List<String> whiteList = null;
            String DATA_FILE_NAME = "Clicks.csv";

            try {
                Config collectionConfig = new NoOptionsConfig(collection);
                File logDir = new File(collectionConfig.getCollectionRoot() + File.separator + "live" + File.separator
                        + "log");

                if (!restoreCollectionStructures(collectionConfig)) {
                    long startTime = System.currentTimeMillis();

                    File data = new File(logDir + File.separator + DATA_FILE_NAME);
                    System.out.println("Processing raw data from file: " + data);

                    String whiteListConfig = collectionConfig.value("recommender.whitelist", "");
                    if (!("").equals(whiteListConfig)) {
                        String[] elements = whiteListConfig.split(",");
                        whiteList = new ArrayList<>(Arrays.asList(elements));
                    }

                    DataModelUtils.loadData(data, whiteList, itemUserMap, userIDPreferencesMap, userSessionsMap,
                            IDStringMap, itemFrequencyMap, memoryIDMigrator);

                    // Create the corresponding Mahout data structure from the map
                    FastByIDMap<PreferenceArray> preferencesOfUsersFastMap = new FastByIDMap<>();
                    for(Entry<Long, List<Preference>> entry : userIDPreferencesMap.entrySet()) {
                        preferencesOfUsersFastMap.put(entry.getKey(), new GenericUserPreferenceArray(entry.getValue()));
                    }

                    // Create a data model and checkpoint all key data structures to disk
                    dataModel = new GenericDataModel(preferencesOfUsersFastMap);
                    checkpointCollection(collectionConfig);
                    long timeTaken = (System.currentTimeMillis() - startTime);
                    System.out.println("Finished processing raw data from file - took " + timeTaken + "ms");
                }

                // Instantiate the recommender
                recommender = new GenericBooleanPrefItemBasedRecommender(dataModel, new LogLikelihoodSimilarity(dataModel));
            } catch (Exception exception) {
                log.log(Level.WARNING, "Exception starting up recommender", exception);
            }
        }
    }

    /**
     * Return a list of recommendations for the given item, limited to a maximum number of recommendations and
     * scoped to the given set of patterns.
     *
     * @param itemName Name of item e.g. URL address
     * @param maxRecommendations maximum number of recommendations to return (less than 1 means unlimited)
     * @param scopes list of scope patterns e.g. cmis.csiro.au,-vic.cmis.csiro.au (empty list means no scopes applied)
     * @return List of recommendations (which may be empty)
     * @throws Exception
     */
	public synchronized List<ItemTuple> recommendThingsForItem(String itemName, int maxRecommendations,
                                                  List<String> scopes) throws Exception {
		List<ItemTuple> recommendations = new ArrayList<>();
		List<RecommendedItem> items = new ArrayList<>();

		try {
			long itemID = memoryIDMigrator.toLongID(itemName);
			
			try {
                items = recommender.mostSimilarItems(itemID, MAX_MODEL_RECOMMENDATIONS);
			}
			catch (NoSuchItemException exception) {
			    System.out.println("Exception finding items for: " + itemName + " Exception: " + exception);
			}

            int i = 0;
			for (RecommendedItem item : items) {
                long recommendedItemID = item.getItemID();
                String itemValue = IDStringMap.get(recommendedItemID);

                if (maxRecommendations > 0 && i >= maxRecommendations) {
                    break;
                }

                if (itemValue != null && ItemUtils.inScope(itemValue, scopes)) {
                    ItemTuple itemTuple = new ItemTuple(itemValue, item.getValue());
                    recommendations.add(itemTuple);
                    i++;
                }
			}
		} catch (TasteException e) {
			log.log(Level.SEVERE, "Error during retrieval of recommendations for item: " + itemName, e);
			throw e;
		}
		return recommendations;
	}

    /**
     * Return true if the given item name is know by the system.
     * @param itemName item name to check
     * @return true if item is known
     */
    public synchronized boolean knownItem(String itemName) {
        boolean known = false;

        Set<String> users = itemUserMap.get(itemName);

        if (users != null) {
            known = true;
        }

        return known;
    }

	/**
	 * Return a set of sessions (list of preferences) that the given item appears in.
	 * @param itemName Name of item
	 * @return set of sessions (which may be empty)
	 */
    public synchronized Set<List<PreferenceTuple>> getSessions(String itemName) {
        Set<List<PreferenceTuple>> sessions = new HashSet<>();
        Set<String> users = itemUserMap.get(itemName);

        if (users != null && !users.isEmpty()) {
            for (String user : users) {
                List<PreferenceTuple> session = userSessionsMap.get(user);

                if (session != null) {
                    sessions.add(session);
                }
            }
        }
        else {
            System.out.println("No users found for item: " + itemName);
        }

        return sessions;
    }

    /**
     * Return the number of times a preference was expressed for this item.
     * @param itemName name or ID of item to look up
     * @return number of preferences (-1 if not available)
     */
    public synchronized long getNumPreferences(String itemName) {
        long value = -1;
        MutableLong frequency = itemFrequencyMap.get(itemName);

        if (frequency != null) {
            value = frequency.longValue();
        }

        return value;
    }
}