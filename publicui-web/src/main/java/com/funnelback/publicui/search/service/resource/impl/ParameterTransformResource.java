package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.search.model.collection.paramtransform.ParamTransformRuleFactory;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;

/**
 * Parses a file containing parameter (CGI) transforms
 * and returns a list of {@link TransformRule}.
 */
@Log4j
public class ParameterTransformResource extends AbstractSingleFileResource<List<TransformRule>> {
    
    public ParameterTransformResource(File file) {
        super(file);
    }

    @Override
    public List<TransformRule> parse() throws IOException {
        log.debug("Loading parameter transform rules from '"+file.getAbsolutePath()+"'");
        String[] rules = new SimpleFileResource(file).parse();
        return ParamTransformRuleFactory.buildRules(rules);
    }

}
