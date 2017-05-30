package com.funnelback.publicui.streamedresults.datafetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.streamedresults.ResultDataFetcher;

public class XJPathResultDataFetcher implements ResultDataFetcher<List<CompiledExpression>> {

    @Override
    public List<CompiledExpression> parseFields(List<String> fields) {
        return fields.stream().map(JXPathContext::compile).collect(Collectors.toList());
    }

    @Override
    public List<Object> fetchFieldValues(List<CompiledExpression> compiledExpressions, Result result) {
        JXPathContext context = JXPathContext.newContext(result);
        context.setLenient(true);
        List<Object> data = new ArrayList<>(compiledExpressions.size());
        for(CompiledExpression exp : compiledExpressions) {
            data.add(exp.getValue(context));
        }
        return data;
    }

}