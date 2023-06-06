package com.funnelback.plugin.docs.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
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

    @Getter
    private final String type;
}
