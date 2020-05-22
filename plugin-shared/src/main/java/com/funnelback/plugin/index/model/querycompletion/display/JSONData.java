package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class JSONData implements AutoCompletionDisplay {
    @NonNull private String json;

    @Override public String getContent() {
        return json;
    }

    @Override public String getTypeCode() {
        return "J";
    }
}
