package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;

@AllArgsConstructor
@Getter
public class OpenUrl implements AutoCompletionAction {
    @NonNull private URI urlToOpen;

    @Override public String getContent() {
        return urlToOpen.toString();
    }

    @Override public String getTypeCode() {
        return "U";
    }
}
