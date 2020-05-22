package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Default implements AutoCompletionDisplay {
    @Override public String getContent() {
        return "";
    }

    @Override public String getTypeCode() {
        return "";
    }
}
