package com.funnelback.plugin.details.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class PluginConfigKeyConditional<T> {
    @NonNull private final String associatedKeyId;
    @NonNull private final List<T> associatedKeyValueInList;
}
