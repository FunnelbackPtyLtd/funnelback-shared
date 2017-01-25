package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.extern.log4j.Log4j2;

import com.funnelback.publicui.search.model.collection.paramtransform.ParamTransformRuleFactory;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

/**
 * Parses a file containing parameter (CGI) transforms
 * and returns a list of {@link TransformRule}.
 */
@Log4j2
public class ParameterTransformResource extends AbstractSingleFileResource<List<TransformRule>> {
    
    public ParameterTransformResource(File file) {
        super(file);
    }

    @Override
    public List<TransformRule> parseResourceOnly() throws IOException {
        log.debug("Loading parameter transform rules from '"+file.getAbsolutePath()+"'");
        String[] rules = new SimpleFileResource(file).parseResourceOnly();
        return ParamTransformRuleFactory.buildRules(rules);
    }

}
