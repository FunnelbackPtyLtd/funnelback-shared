package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class ExtendQuery implements AutoCompletionAction {
    @NonNull private String querySuffixToAdd;
}
