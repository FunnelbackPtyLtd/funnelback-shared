package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class HTMLFragment implements AutoCompletionDisplay {
    @NonNull private String html;

    @Override public String getContent() {
        return html;
    }

    @Override public String getTypeCode() {
        return "H";
    }
}
