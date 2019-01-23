package com.funnelback.publicui.utils;

import org.springframework.stereotype.Component;

import com.funnelback.common.file.ReadOnlyFileService;
import com.funnelback.springmvc.utils.ConfFileService;

@Component
public class PublicUIFileService extends ReadOnlyFileService // We shouldn't need to write to conf under the public UI 
                                implements ConfFileService {

}
