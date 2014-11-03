package com.funnelback.publicui.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Properties;

import lombok.extern.log4j.Log4j;

import com.funnelback.common.config.Collection;
import com.funnelback.common.config.Collections;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.policy.SimpleLoadingPolicy;
import com.funnelback.crawler.config.CrawlerConfig;

@Log4j
public class UrlStatus {

    public static enum MatchResult {
        MATCHES,
        FAILS,
        NOT_RELEVENT;
    }
    
    public static MatchResult UrlMatchesCrawlerIncludeExcludePattern(File searchHome, String collection, String url) {
        
        Config config;
        try {
            config = new NoOptionsConfig(searchHome, collection);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(Collection.Type.push2.toString().equals(config.value(Keys.COLLECTION_TYPE))){
            return MatchResult.NOT_RELEVENT;
        } 
        if(Collection.Type.meta.toString().equals(config.value(Keys.COLLECTION_TYPE))){
            MatchResult best = MatchResult.FAILS;
            for(String c : Collections.getComponentCollections(searchHome, collection)) {
                MatchResult mResult = UrlMatchesCrawlerIncludeExcludePattern(searchHome, c, url);
                if(MatchResult.MATCHES.equals(mResult)){
                    return mResult;
                } else if (MatchResult.NOT_RELEVENT.equals(mResult)){
                    best = mResult;
                }
            }
            return best;
        }
        SimpleLoadingPolicy loadingPolicy = new SimpleLoadingPolicy();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(bos);//will use default encoding.
        try  {
            
            File configFile = new File(config.getConfigDirectory(), Files.COLLECTION_FILENAME);
            Properties properties = CrawlerConfig.readCollectionConfiguration(configFile.getAbsolutePath(), 
                searchHome.getAbsolutePath());
            int verbose = 0;
            if(log.isDebugEnabled()) {
                verbose=1;
            }
            loadingPolicy.setLoadingRules(properties, verbose, printWriter);
            boolean accepted = loadingPolicy.acceptableAddress(url);
            log.debug(new String(bos.toByteArray()));
            if(accepted) {
                return MatchResult.MATCHES;
            } else {
                return MatchResult.FAILS;
            }
        } catch (Throwable t) {
            log.error(new String(bos.toByteArray())); //will use default encoding.
            throw t;
        }
    }
    
}
