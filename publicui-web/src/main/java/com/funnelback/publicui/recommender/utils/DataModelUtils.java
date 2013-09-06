package com.funnelback.publicui.recommender.utils;

import com.funnelback.publicui.recommender.FBRecommender;
import com.funnelback.publicui.recommender.tuple.PreferenceTuple;

import org.apache.commons.lang.mutable.MutableLong;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.MemoryIDMigrator;
import org.apache.mahout.cf.taste.model.Preference;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DataModelUtils {
    public static final int MAX_CLICKS_PER_SESSION = 8;

    // Private constructor to avoid unnecessary instantiation of the class
    private DataModelUtils() {
    }

    /**
     * Store the given list of preferences in the data model. Will refuse to store
     * the list if its size is greater than MAX_CLICKS_PER_SESSION.
     *
     * @param preferenceTuples     list of preference tuples.
     * @param itemUserMap          map between items and users
     * @param userIDPreferencesMap map between user IDs and their list of preferences
     * @param sessionsMap          map between users and sessions
     * @param IDStringMap          map between Mahout user and item IDs and their String forms
     * @param itemFrequencyMap     map between item and frequency count for that item
     * @param memoryIDMigrator     Mahout in-memory ID migrator
     */
    public static void storePreferences(List<PreferenceTuple> preferenceTuples, Map<String, Set<String>> itemUserMap,
            Map<Long, List<Preference>> userIDPreferencesMap, Map<String, List<PreferenceTuple>> sessionsMap,
            Map<Long, String> IDStringMap, Map<String, MutableLong> itemFrequencyMap, MemoryIDMigrator memoryIDMigrator) {
        String sessionID;

        if (preferenceTuples.size() > MAX_CLICKS_PER_SESSION) {
            System.out.println("Warning: Refusing to store session. Size: "
                    + preferenceTuples.size() + " > " + MAX_CLICKS_PER_SESSION);
        } else {
            if (!preferenceTuples.isEmpty()) {
                // Store a mapping between the session ID and all preferenceTuples for this session
                sessionID = preferenceTuples.get(0).getUserID();
                sessionsMap.put(sessionID, preferenceTuples);
            }

            for (PreferenceTuple tuple : preferenceTuples) {
                String user = tuple.getUserID();
                String item = tuple.getItemID();

                // create a long from the user name & store it
                long userLong = memoryIDMigrator.toLongID(user);
                IDStringMap.put(userLong, user);

                // create a long from the item & store it
                long itemLong = memoryIDMigrator.toLongID(item);
                IDStringMap.put(itemLong, item);

                List<Preference> userPrefList;

                // If we already have a userPrefList use it
                // otherwise create a new one.
                if ((userPrefList = userIDPreferencesMap.get(userLong)) == null) {
                    userPrefList = new ArrayList<>();
                    userIDPreferencesMap.put(userLong, userPrefList);
                }
                // add the 'like' that we just found to this user
                userPrefList.add(new GenericPreference(userLong, itemLong, 1));

                System.out.println("Stored: " + tuple);

                // Store details for this item e.g. which session/user it belongs to
                if (itemUserMap.containsKey(item)) {
                    itemUserMap.get(item).add(user);
                } else {
                    Set<String> sessions = new HashSet<>();
                    sessions.add(user);
                    itemUserMap.put(item, sessions);
                }

                // Increment the frequency count for this item
                MutableLong frequency = itemFrequencyMap.get(item);

                if (frequency == null) {
                    itemFrequencyMap.put(item, new MutableLong(1));
                }
                else {
                    frequency.increment();
                }
            }

            System.out.println("============================================");
        }
    }

    /**
     *
     *
     * @param data File containing preference data (e.g. click records, "like" records etc).
     * @param whiteList List of strings which item strings must contain. May be null.
     * @param itemUserMap map between items and users
     * @param userIDPreferencesMap map between user IDs and their list of preferences
     * @param sessionsMap map between users and sessions
     * @param IDStringMap map between Mahout user and item IDs and their String forms
     * @param itemFrequencyMap
     *@param memoryIDMigrator Mahout in-memory ID migrator  @throws java.io.IOException
     */
    public static void loadData(File data, List<String> whiteList, Map<String, Set<String>> itemUserMap,
            Map<Long, List<Preference>> userIDPreferencesMap, Map<String, List<PreferenceTuple>> sessionsMap,
            Map<Long, String> IDStringMap, Map<String, MutableLong> itemFrequencyMap,
            MemoryIDMigrator memoryIDMigrator) throws IOException {
    	BufferedReader reader = null;
        ArrayList<PreferenceTuple> currentPreferences = new ArrayList<>();
        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy");

    	try {
    		reader = new BufferedReader(new InputStreamReader(new FileInputStream(data), "UTF-8"));
            Calendar epochCal = new GregorianCalendar();
            epochCal.setTimeInMillis(0);

    		String line = "";
            String previousUser = "";
            Date sessionStart = epochCal.getTime();

    		while((line = reader.readLine()) != null) {
    		    Date timeStamp;
                String[] elements = line.split(",");

                if (elements.length == 5) {
                    String user = elements[0].trim();
                    String item = elements[1].trim();

                    if (whiteList != null) {
                        boolean match = false;

                        for (String required : whiteList) {
                            if (item.contains(required.trim())) {
                                match = true;
                                break;
                            }
                        }

                        if (!match) {
                            System.out.println("Skipping record where item doesn't match recommender.whitelist elements: " + line);
                            continue;
                        }
                    }

                    String dateTime = elements[2].trim();
                    String host = elements[3].trim();
                    String query = elements[4].trim();

                    if (host.toLowerCase().contains("proxy")) {
                        System.out.println("Skipping proxy: " + line);
                        continue;
                    }

                    if (query == null || query.equals("")) {
                        System.out.println("Skipping record with no query: " + line);
                        continue;
                    }

                    try {
                        timeStamp = df.parse(dateTime);
                    }
                    catch (ParseException parseException) {
                        System.out.println(parseException + " - Line: " + line);
                        continue;
                    }

                    if (sessionStart != null) {
                        long interval = timeStamp.getTime() - sessionStart.getTime();

                        if (interval > FBRecommender.TIME_WINDOW_SIZE) {
                            user = user + "-" + timeStamp.getTime();
                        }
                        else {
                            user = user + "-" + sessionStart.getTime();
                        }
                    }

                    PreferenceTuple preferenceTuple =
                            new PreferenceTuple(user, item, timeStamp, host, query);

                    if (!user.equals(previousUser)) {
                        // Store current preferences (which may be empty)
                        storePreferences(currentPreferences, itemUserMap, userIDPreferencesMap, sessionsMap,
                                IDStringMap, itemFrequencyMap, memoryIDMigrator);

                        // Start a new preferences list for this new user and session (which may contain only one entry)
                        currentPreferences = new ArrayList<>();
                        currentPreferences.add(preferenceTuple);

                        // Record user and session start
                        previousUser = user;
                        sessionStart = timeStamp;
                    }
                    else {
                        // Add the preference for this user to the list
                        currentPreferences.add(preferenceTuple);
                    }
                }
                else {
                    System.out.println("Bad input line: " + line);
                }
    		}
    	}
    	finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException exception) {
                    // Ignore
                }
            }
    	}


        // Flush the final list
        storePreferences(currentPreferences, itemUserMap, userIDPreferencesMap, sessionsMap,
                IDStringMap, itemFrequencyMap, memoryIDMigrator);
    }
}
