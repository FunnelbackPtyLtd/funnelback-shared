package com.funnelback.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.internal.StringUtil;

import java.net.*;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class URIHandlingUtils {
    private static final String UNIVERSAL_IP = "169.254.169.254";

    /**
     * create URI based on the incoming URL string
     * <p>
     * NOTES: Java.NET.URI class is more restrictive than the URL class when it comes to the special characters.
     *</p>
     * <p>
     * RFC1630 (T. Berners-Lee, CERN 1994) classifies the vertical bar (called "v-line" in the spec)
     * as a "national" character:
     * national { | } | v-line | [ | ] | \ | ^ | ~
     * And then says: The "national" and "punctuation" characters do not appear in any productions
     * and therefore may not appear in URIs.
     * </p>
     * <p>
     * This issue was identified in ticket https://squizgroup.atlassian.net/browse/DXPSUP-515
     * So a raw string needs to be converted to URL object, then converted to URI to ensure those special characters accepted by URL but not URI  were encoded.
     *</p>
     * @param urlString The URL string used to create the URI object
     * @return The created URI object
     */
    public static URI create(String urlString)  {
        if (urlString == null || StringUtil.isBlank(urlString)) {
            return null;
        }

        try {
            URL newURL = new URL(urlString);
            validateUniversalIpAddress(newURL);
            return new URI(newURL.getProtocol(), newURL.getUserInfo(), newURL.getHost(), newURL.getPort(), newURL.getPath(), newURL.getQuery(), newURL.getRef());

        } catch (MalformedURLException | URISyntaxException e) {
            // it is not an absolute URL string, so create a new URI based on the raw string with all special characters encoded
            return URI.create(URLEncoder.encode(urlString, StandardCharsets.UTF_8));
        }
    }

    /**
     * Ensure cloud provider (i.e. AWS) metadata service endpoint cannot be called
     * See https://squizgroup.atlassian.net/browse/SEARCH-1204
     */
    private static void validateUniversalIpAddress(URL url) throws IllegalArgumentException {
        if (url.getHost().startsWith(UNIVERSAL_IP)) {
            throw new IllegalArgumentException("Access to cloud provider metadata service is not allowed");
        }
    }
}