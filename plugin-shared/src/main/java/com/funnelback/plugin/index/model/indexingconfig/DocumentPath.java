package com.funnelback.plugin.index.model.indexingconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class DocumentPath {
    @NonNull public String path;
}
