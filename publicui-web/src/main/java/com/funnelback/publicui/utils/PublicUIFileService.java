package com.funnelback.publicui.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.file.FunnelbackFilePath;
import com.funnelback.common.file.ReadOnlyFileService;
import com.funnelback.common.file.cache.FunnelbackFilePathCachedResourceEntryValidator;
import com.funnelback.common.file.cache.haschanged.HasFileChanged;
import com.funnelback.common.file.cache.haschanged.StandardHasFileChanged;
import com.funnelback.springmvc.utils.ConfFileService;

@Component
public class PublicUIFileService extends ReadOnlyFileService // We shouldn't need to write to conf under the public UI 
                                implements ConfFileService {

    @Autowired
    private FunnelbackFilePathCachedResourceEntryValidator cachedEntryValidator;
    
    @Override
    public HasFileChanged createHasFileChanged(FunnelbackFilePath funnelbackFilePath) {
        return new StandardHasFileChanged(funnelbackFilePath, cachedEntryValidator);
    }
}
