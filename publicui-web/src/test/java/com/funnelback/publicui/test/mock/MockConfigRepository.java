package com.funnelback.publicui.test.mock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.junit.Assume;

import com.funnelback.common.config.Config;
import com.funnelback.config.configtypes.server.ServerConfigReadOnly;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

public class MockConfigRepository implements ConfigRepository {

    private Map<String, Collection> collections = new HashMap<String, Collection>();
    @Getter private Map<GlobalConfiguration, Map<String, String>> globalConfigs = new HashMap<GlobalConfiguration, Map<String, String>>();
    @Getter private final Map<String, String> translations = new HashMap<String, String>();
    @Getter @Setter private File xslTemplate;
    private Map<String, Map<String, String>> extraSearchesConfigurations = new HashMap<>();
    
    @Getter @Setter private Config globalConfiguration;
    
    @Getter @Setter private ServerConfigReadOnly serverConfig;
        
    @Override
    public Collection getCollection(String collectionId) {
        return collections.get(collectionId);
    }

    @Override
    public List<Collection> getAllCollections() {
        return new ArrayList<Collection>(collections.values());
    }

    @Override
    public List<String> getAllCollectionIds() {
        return new ArrayList<String>(collections.keySet());
    }
    
    public void addCollection(Collection c) {
        collections.put(c.getId(), c);
    }
    
    public void removeCollection(String collectionId) {
        collections.remove(collectionId);
    }
    
    public void removeAllCollections() {
        collections.clear();
    }
    
    @Override
    public Map<String, String> getGlobalConfigurationFile(GlobalConfiguration conf) {
        return globalConfigs.get(conf);
    }

    @Override
    public String[] getForms(String collectionId, String profileId) {
        return new String[]{"simple"};
    }
    
    @Override
    public Map<String, String> getExtraSearchConfiguration(Collection collection, String extraSearchId) {
        return extraSearchesConfigurations.get(collection.getId()+":"+extraSearchId);
    }
    
    public void addExtraSearchConfiguration(String collectionId, String extraSearchId, Map<String, String> m) {
        extraSearchesConfigurations.put(collectionId+":"+extraSearchId, m);
    }

    private final static Pattern WIN_PATH_PATTERN = Pattern.compile("([^;]*?Perl[^;]*?)[;$]");
    @Override
    public String getExecutablePath(String exeName) {
        // always returns perl.
        
        File perlBin = null;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Look in PATH for Perl
            String path = System.getenv("PATH");
            
            // The folder containing Perl will be named something like
            // C:\Perl\... or C:\funnelback\wbin\ActivePerl\...
            if (path.contains("Perl")) {
                // Try to extract it
                Matcher m = WIN_PATH_PATTERN.matcher(path);
                while (m.find()) {
                    // The PATH contains 2 entries for Perl, one in /site/bin
                    // and the other one in /bin
                    if (! m.group(1).contains("site")) {
                        perlBin = new File(m.group(1), "perl.exe");
                        break;
                    }
                }
            }
        } else {
            // Linux boxes always have Perl
            perlBin = new File("/usr/bin/perl");
        }
        
        // Skip the test if we haven't found a Perl interpreter
        // of if it cannot be executed.
        Assume.assumeTrue(perlBin != null && perlBin.canExecute());
        System.out.println("Will use the following Perl binary: '"+perlBin.getAbsolutePath()+"'");
        return perlBin.getAbsolutePath();
    }

    @Override
    public Map<String, String> getTranslations(String collectionId,
            String profileId, Locale locale) {
        return translations;
    }

    @Override
    public File getXslTemplate(String collectionId, String profileId) {
        return xslTemplate;
    }
    
}

