package com.funnelback.publicui.testutils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;

public class TestUtils {

private final static File READ_ONLY_SEARCH_HOME = new File("src/test/resources/dummy-search_home");
    
    public static File getWritableSearchHome(String className, String  methodName) throws Exception{
        File newSearchHome = new File("target" + File.separator + className  + File.separator + methodName 
            + File.separator + "search_home");
        FileUtils.deleteDirectory(newSearchHome);
        FileUtils.copyDirectory(READ_ONLY_SEARCH_HOME, newSearchHome);
        return newSearchHome;
    }
    
    public static File getWritableTestDir(String className, String  methodName) throws Exception{
        File writabletestDir = new File("target" + File.separator + className + File.separator + methodName);
        FileUtils.deleteDirectory(writabletestDir);
        writabletestDir.mkdirs();
        return writabletestDir;
    }
    
    public static File getReadOnlySearchHome() {
        return READ_ONLY_SEARCH_HOME;
    }
    
    public static File getConfigDir(File searchHome, String collection){
        return new File(searchHome,DefaultValues.FOLDER_CONF + File.separator + collection);
    }
    
    public static File getProfileDir(File searchHome, String collection, String profile) {
        return new File(getConfigDir(searchHome, collection), profile);
    }
    
    public static File getDataDir(File searchHome, String collection) {
        return new File(searchHome,DefaultValues.FOLDER_DATA + File.separator + collection);
    }
    
    public static void createVeryBasicCollection(File searchHome, String collectionId, Map<String, String> params) 
            throws IOException{
        File confDir = getConfigDir(searchHome, collectionId);
        File collectionCfg = new File(confDir, Files.COLLECTION_FILENAME);
        
        FileUtils.writeStringToFile(collectionCfg, 
            defaultCollectionCfgString + mapToString(params), 
            "UTF-8");
        
        getDataDir(searchHome, collectionId).mkdir();
    }
    
    private static final String defaultCollectionCfgString = 
        "collection_root=$SEARCH_HOME/data/$COLLECTION_NAME\n"
        + "collection=$COLLECTION_NAME\n";
    
    public static String mapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for(java.util.Map.Entry<String, String> e : map.entrySet()){
            sb.append(e.getKey())
                .append("=")
                .append(e.getValue())
                .append("\n");
        }
        return sb.toString();
    }
}
