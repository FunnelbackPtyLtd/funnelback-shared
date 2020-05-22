package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class PlainText implements AutoCompletionDisplay {
    @NonNull private String text;

    @Override public String getContent() {
        return text;
    }

    @Override public String getTypeCode() {
        return "T";
    }
}
