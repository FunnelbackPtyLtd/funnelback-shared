package com.funnelback.publicui.recommender.utils;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.recommender.tuple.ItemTuple;
import com.funnelback.publicui.recommender.tuple.PreferenceTuple;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for dealing with recommended items.
 * @author fcrimmins@funnelback.com
 */
public final class ItemUtils {

    // Private constructor to avoid unnecessary instantiation of the class
    private ItemUtils() {
    }

    /**
     * Return a {@link Map} of {@link DocInfo} objects, keyed by the document {@link URI}
     *
     * @param items list of item tuples to get document information for
     * @param collection name of collection to query
     * @param metadataClass metadata class to get information on
     * @return map<URI, DocInfo></URI,>
     */
    public static Map<URI, DocInfo> getDocInfoMap(List<ItemTuple> items, String collection, String metadataClass) {
        Map<URI, DocInfo> docInfoMap = new HashMap<>();
        List<DocInfo> dis = getDocInfoList(items, collection);

        for (DocInfo docInfo : dis) {
            docInfoMap.put(docInfo.getUri(), docInfo);
        }

        return docInfoMap;
    }

    public static String getTitle(String url, String collection) {
        String title = "";
        List<ItemTuple> items = new ArrayList<>();
        ItemTuple keyItem = new ItemTuple(url, 0);
        items.add(keyItem);

        List<DocInfo> dis = getDocInfoList(items, collection);

        if (!dis.isEmpty()) {
            DocInfo docInfo = dis.get(0);
            title = docInfo.getTitle();
        }

        return title;
    }

    /**
     * Get a list of item tuples from the given list of preferences.
     * @param preferences list of preference tuples
     * @return List of item tuples (which may be empty)
     */
    public static List<ItemTuple> getItemsFromPreferences(List<PreferenceTuple> preferences) {
        List<ItemTuple> items = new ArrayList<>();

        for (PreferenceTuple preference : preferences) {
            ItemTuple item = new ItemTuple(preference.getItemID(), 0);
            items.add(item);
        }

        return items;
    }

    /**
     * Return a list of DocInfo objects for the given URL items in the given collection.
     * Document information for any URLs which are not in the index will not be
     * present in the returned list.
     *
     *
     * @param items list of URL items
     * @param collection collection to get document information from
     * @return a list of DocInfo objects for each URL that was in the collection
     */
    public static List<DocInfo> getDocInfoList(List<ItemTuple> items, String collection) {
        List<DocInfo> dis;

        Config collectionConfig = new NoOptionsConfig(collection);
        File indexStem = new File(collectionConfig.getCollectionRoot() + File.separator + "live" + File.separator
                + "idx" + File.separator + "index");

        URI[] addresses = new URI[items.size()];
        int i = 0;
        for (ItemTuple item : items) {
            addresses[i] = URI.create(item.getItemID());
            i++;
        }

        dis = new PadreConnector(indexStem).docInfo(addresses).withMetadata(DocInfoQuery.ALL_METADATA).fetch();

        return dis;
    }

    /**
     * Return true if the given item is considered "in scope" based on the given list
     * of scope patterns (which may be empty).
     * @param item String to test for display
     * @param scopes list of scope patterns e.g. cmis.csiro.au,-vic.cmis.csiro.au
     * @return true if item should be displayed
     */
    public static boolean inScope(String item, List<String> scopes) {
        boolean inScope = false;

        if (scopes != null && scopes.size() > 0) {
            for (String scopePattern : scopes) {
                if (scopePattern.startsWith(("-"))) {
                    // Negative scope pattern i.e. -handbook.curtin.edu.au
                    scopePattern = scopePattern.substring(1, scopePattern.length());

                    if (item.contains(scopePattern)) {
                        inScope = false;
                        break;
                    }
                }
                else if (item.contains(scopePattern)) {
                    inScope = true;
                }
            }
        }
        else {
            inScope = true;
        }

        return inScope;
    }
}
