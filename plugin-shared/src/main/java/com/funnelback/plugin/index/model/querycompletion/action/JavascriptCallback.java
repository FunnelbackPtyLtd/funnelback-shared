package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class JavascriptCallback implements AutoCompletionAction {
    @NonNull private String callbackCode;

    @Override public String getContent() {
        return callbackCode;
    }

    @Override public String getTypeCode() {
        return "C";
    }
}
