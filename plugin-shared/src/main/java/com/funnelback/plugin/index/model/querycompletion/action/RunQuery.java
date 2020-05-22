package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class RunQuery implements AutoCompletionAction {
    @NonNull private String queryToRun;

    @Override public String getContent() {
        return queryToRun;
    }

    @Override public String getTypeCode() {
        return "Q";
    }
}
