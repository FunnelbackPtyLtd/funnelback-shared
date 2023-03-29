package com.funnelback.plugin.docs.model;

import lombok.RequiredArgsConstructor;

/**
 * Source documentation meta tag "hc-audience"
 */
@RequiredArgsConstructor
public enum Audience {
    ADMINISTRATOR("Administrator"),
    CONTENT_EDITOR("Content editor"),
    DEVELOPER("Developer"),
    SITE_BUILDER("Site builder");

    private final String type;
}
