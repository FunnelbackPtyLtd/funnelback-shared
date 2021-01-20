package com.funnelback.plugin.index.model.indexingconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class UrlPath {
    @NonNull public String path;
}
