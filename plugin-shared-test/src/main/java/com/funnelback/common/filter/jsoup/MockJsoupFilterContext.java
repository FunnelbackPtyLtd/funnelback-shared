package com.funnelback.common.filter.jsoup;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MockJsoupFilterContext implements FilterContext {

    @Getter private final SetupContext setup;    
    
    @Getter private final Document document;
    
    @Getter private final Multimap<String, String> additionalMetadata = HashMultimap.create();
    
    @Getter private final Map<String, Object> customData = new HashMap<String, Object>();  
    
    public MockJsoupFilterContext(String html) {
        this(new MockJsoupSetupContext(), Jsoup.parse(html));
    }
    
    public MockJsoupFilterContext(Document doc) {
        this(new MockJsoupSetupContext(), doc);
    }
}
