package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class JavaScriptCallback implements AutoCompletionDisplay {
    @NonNull private String code;

    @Override public String getContent() {
        return code;
    }

    @Override public String getTypeCode() {
        return "C";
    }
}
