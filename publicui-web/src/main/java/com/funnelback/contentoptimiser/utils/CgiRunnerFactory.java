package com.funnelback.contentoptimiser.utils;

import java.io.File;

import org.springframework.stereotype.Component;

import com.funnelback.common.utils.cgirunner.CgiRunner;
import com.funnelback.common.utils.cgirunner.DefaultCgiRunner;

@Component
public class CgiRunnerFactory {
    
    public CgiRunner create(File pathToCgi, File pathToPerl) {
        return new DefaultCgiRunner(pathToCgi,pathToPerl);
    }
}
