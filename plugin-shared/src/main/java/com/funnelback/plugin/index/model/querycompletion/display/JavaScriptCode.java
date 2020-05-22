package com.funnelback.plugin.index.model.querycompletion.display;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class JavaScriptCode implements AutoCompletionDisplay {
    @NonNull private String code;
}
