package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.jxpath.CompiledExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnelback.common.Environment.FunnelbackVersion;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.streamedresults.CommaSeparatedList;
import com.funnelback.publicui.streamedresults.CommaSeparatedListEditor;
import com.funnelback.publicui.streamedresults.DataConverter;
import com.funnelback.publicui.streamedresults.PagedSearcher;
import com.funnelback.publicui.streamedresults.ResultFields;
import com.funnelback.publicui.streamedresults.converters.CSVDataConverter;
import com.funnelback.publicui.streamedresults.converters.JSONDataConverter;
import com.funnelback.publicui.streamedresults.datafetcher.XJPathResultDataFetcher;
import com.funnelback.publicui.streamedresults.outputstreams.CloseIgnoringOutputStream;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.google.common.collect.ImmutableMap;

import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
public class StreamResultsController {

    @Autowired
    private SearchController searchController;
    
    @Autowired
    private ExecutionContextHolder executionContextHolder;
    
    @Autowired
    private LocaleResolver localeResolver;
    
    @Autowired
    private FunnelbackVersion funnelbackVersion;
    
    @Autowired
    private SearchTransactionProcessor processor;
    
    @Autowired
    private JSONDataConverter JSONDataConverter;
    
    @Autowired
    private CSVDataConverter CSVDataConverter;
    
    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
        binder.registerCustomEditor(CommaSeparatedList.class, new CommaSeparatedListEditor());
    }
    
    private DataConverter<Object> getDataConverterFromExtension(String ext) {
        if(ext.equals("json")) {
            return (DataConverter) this.JSONDataConverter;
        }
        
        if(ext.equals("csv")) {
            return (DataConverter) this.CSVDataConverter;
        }
        
        throw new RuntimeException();
        
    }
    
    @RequestMapping("/fun.*")
    public void acknowledgementCounts(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required=false) CommaSeparatedList fields,
            @RequestParam(required=false) CommaSeparatedList fieldnames,
            @RequestParam(required=false, defaultValue="true") boolean optimisations,
            @Valid SearchQuestion question,
            @ModelAttribute SearchUser user) throws Exception {
        
        ResultFields resultFields = new ResultFields(Optional.of(fields).map(CommaSeparatedList::getList), Optional.of(fieldnames).map(CommaSeparatedList::getList));
        
        if (question.getCollection() != null) {
            SearchQuestionBinder.bind(executionContextHolder.getExecutionContext(), request, question, localeResolver, funnelbackVersion);
            
            PagedSearcher pageSearcher = new PagedSearcher(question, !optimisations);
            
            
            DataConverter<Object> dataConverter = getDataConverterFromExtension(FilenameUtils.getExtension(request.getRequestURI()));
            
            if(dataConverter == null) {
                log.warn("Search called with an unknown extension '"+request.getRequestURL()+"'.");
                throw new RuntimeException("Unknown extension: " + FilenameUtils.getExtension(request.getRequestURI()));
            }
            
            response.setContentType(dataConverter.getContentType());
            
            Object writter = dataConverter.createWritter(new CloseIgnoringOutputStream(response.getOutputStream()));
            
            dataConverter.writeHead(resultFields.getFieldNames(), writter);
            
            List<CompiledExpression> compiledExpressions = RESULT_DATA_FETCHER.parseFields(resultFields.getXPaths());
            
            AtomicBoolean isFirst = new AtomicBoolean(true);
            
            pageSearcher.runOnEachPage((q) -> {
                return processor.process(q, user);
            }, (transactionRef) -> {
                SearchTransaction transaction = transactionRef.getValue();
                transactionRef.setValue(null);
                
                List<Result> results = transaction.getResponse().getResultPacket().getResults();
                transaction = null;
               
                try {
                    if(!writeResults(results, dataConverter, writter, resultFields.getFieldNames(), compiledExpressions, isFirst.get())) {
                        isFirst.set(false);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                
            });
            
            dataConverter.writeFooter(writter);
            dataConverter.finished(writter);
            response.getOutputStream().close();
            
        } else {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            response.getOutputStream().close();
        }
        
    }
    
    private static final XJPathResultDataFetcher RESULT_DATA_FETCHER = new XJPathResultDataFetcher();
    
    
    public <T> boolean writeResults(List<Result> results, 
                                    DataConverter<T> dataConverter, 
                                    T writter,
                                    List<String> fieldNames,
                                    List<CompiledExpression> expressions,
                                    boolean isFirst) throws IOException {
        
        // It is likely the case that the request fields produces a list of objects
        // that are smaller in size than the entire Result object. Thus we convert the list
        // of Results to just what is request and NULL out the Results so they can be GCed
        // We do this now because we don't need to wait for the user to download anything
        // before we can start delting.
        List<List<Object>> valuesForResults = gcFriendlyListTraverser(results, 
            (result) -> RESULT_DATA_FETCHER.fetchFeilds(fieldNames, expressions, result));
        
        gcFriendlyListTraverser(valuesForResults, (values) -> {
            try {
                dataConverter.writeRecord(fieldNames, values, writter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        
        
//        for(int i = 0; i < results.size(); i++) {
//            Result result = results.get(i);
//            results.set(i, null); //Null this out we want the memory usage to go down as we process results
//            // If we have 60MB of data sitting in results and the user is only downloading at 100KB/s
//            // then we will hold on to 60MB for much longer than we would like considering how frequent
//            // OOMs are we make an effort to reduce the chance.
//            if(!isFirst) {
//                dataConverter.writeSeperator(writter);
//            }
//            
//            dataConverter.writeRecord(fieldNames, RESULT_DATA_FETCHER.fetchFeilds(fieldNames, expressions, result), writter);
//            
//            isFirst = false;
//            
//            
//        }
        
        return isFirst;
    }
    
    public <E,T> List<T> gcFriendlyListTraverser(List<E> listTOProcess, Function<E, T> converter) {
        // The array of ptrs wont be very big it is the object themselves that is big. 
        List<T> convertedList = new ArrayList<>(listTOProcess.size());
        for(int i = 0; i < listTOProcess.size(); i++) {
            convertedList.add(converter.apply(listTOProcess.get(i)));
            
            // NUll out the object we are done with so the GC may collect it.
            listTOProcess.set(i, null);
        }
        
        return convertedList;
    }
    
}
