package com.funnelback.publicui.search.model.transaction.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * This class contains all the input parameters for a cache request
 * 
 * @since 15.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class CacheQuestion {

    /**
     * {@link Collection} to get the cached document from
     */
    @NonNull
    @Getter @Setter
    private Collection collection;
    
    /**
     * Profile where to lookup the cached copies template to use 
     */
    @NonNull
    @javax.validation.constraints.Pattern(regexp="[\\w-_]+")
    @Getter @Setter private String profile = DefaultValues.DEFAULT_PROFILE;
    
    /**
     * <p>Form (template) to use to render the cached copy</p>
     * 
     * <p>The template is expected to be prefixed with <code>cache.</code>
     * and have the extension <code>.ftl</code>
     */
    @NonNull
    @javax.validation.constraints.Pattern(regexp="[\\w-_]+")        
    @Getter @Setter
    private String form = DefaultValues.DEFAULT_FORM;
    
    /**
     * URL of the document to retrieve the cached copy of
     */
    @Getter @Setter
    private String url;
    
    /**
     * <p>Relative path of the document in the collection storage
     * to access the document.</p>
     * 
     * <p>This is used as a fallback method if the document cannot
     * be found via its URL, for example if the URL has been rewritten</p>
     */
    @Getter @Setter
    private String doc;
    
    /**
     * Offset of the document in the WARC file
     */
    @Getter @Setter
    private long off = 0;
    
    /**
     * Length of the document in the WARC file
     */
    @Getter @Setter
    private int len = -1;

}
