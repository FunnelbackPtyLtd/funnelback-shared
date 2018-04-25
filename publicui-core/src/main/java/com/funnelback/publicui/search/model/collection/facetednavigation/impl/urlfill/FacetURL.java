package com.funnelback.publicui.search.model.collection.facetednavigation.impl.urlfill;

import com.funnelback.common.url.VFSURLUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of="urlForComparison")
@Getter
public class FacetURL {

    /**
     * The URL when it is converted into a form that is URI friendly
     * this includes
     * - converting \\foo\a into smb://foo/a
     * - adding http:// if not host is given
     * - Ensuring the URL end with a slash (for consistency)
     * 
     * The casing is maintained in this.
     */
    private String urlFixed;
    
    /**
     * This is the same as the urlFixed however the casing is lowercased
     * in any part of the URI that is case insensitive.
     * If this does anything other than lowercase the URI users of this
     * class will need to be updated to ensure they don't expect the
     * urlFixed and this to be the same length.
     */
    private String urlForComparison;
    
    public FacetURL(String url) {
        
        url = convertWindowsURLToSmb(url);
        
        url = ensureUrlhasScheme(url);
        
        url = ensurePathEndsWithSlash(url); // Just make sure everything is nice and consistent
        
        this.urlFixed = url;
        // for the comparison URL we need to do some more work.
        this.urlForComparison = prepUrlForComparison(url);
    }
    
    static String convertWindowsURLToSmb(String url) {
        if (url.startsWith(VFSURLUtils.WINDOWS_URL_PREFIX)) {
            // Windows style url \\server\share\folder\file.ext
            // Convert to smb://... so that URLs returned by PADRE will match
          url = VFSURLUtils.SystemUrlToVFSUrl(url);
          if(url.endsWith("//")) {
              url = url.substring(0, url.length()-1);
          }
        }
        return url;
    }
    
    static String ensureUrlhasScheme(String url) {
        
        int schemeSepStart = url.indexOf("://");
        if(schemeSepStart != -1) {
            String scheme = url.substring(0, schemeSepStart);
            // Does the scheme look invalid?
            if(!scheme.contains("/") || scheme.contains(".")) {
                return url;
            }
        }
        
        return "http://" + url; 
    }
    
    static String ensurePathEndsWithSlash(String url) {
        // In facets URLs will not contain ?query= or #fragments parts
        if(url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }
    
    /**
     * May only change the case of letters, if it does otherwise getSelectedItems()
     * will need to change
     * @param url
     * @return
     */
    static String prepUrlForComparison(String url) {
        if(url.toLowerCase().startsWith("smb://")) {
            // In samba the hostname, share and path are all case insensitive.
            // although they will preserve case to show the user.
            return url.toLowerCase();
        } else {
            // In other URLs the schema and domain are case insensitive.
            // lowercase the domain and schema
            int schemeSepStart = url.indexOf("://");
            if(schemeSepStart == -1) {
                // sometimes we wont have http:// because padre drops it in a bunch of places this
                // makes this code risky assume everythig up to the first / is the domain
                schemeSepStart = 0;
            }
            
            // find where the path starts
            int pathStart = url.indexOf('/', schemeSepStart + 3);
            if(pathStart == -1) {
                pathStart = url.length();
            }
            
            String schemeAndDomain = url.substring(0, pathStart);
            String path = url.substring(pathStart, url.length());
            return schemeAndDomain.toLowerCase() + path;
        }
    }
    
    
}
