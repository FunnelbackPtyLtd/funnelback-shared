package com.funnelback.plugin.index.model.querycompletion.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;

@AllArgsConstructor
@Getter
public class OpenUrl implements AutoCompletionAction {
    @NonNull private URI urlToOpen;
}
