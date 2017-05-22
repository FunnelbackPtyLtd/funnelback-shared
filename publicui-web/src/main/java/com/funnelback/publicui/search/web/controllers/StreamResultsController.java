package com.funnelback.publicui.search.web.controllers;

import java.util.List;
import java.util.Optional;

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

import com.funnelback.common.Environment.FunnelbackVersion;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.streamedresults.CommaSeparatedList;
import com.funnelback.publicui.streamedresults.CommaSeparatedListEditor;
import com.funnelback.publicui.streamedresults.DataConverter;
import com.funnelback.publicui.streamedresults.PagedSearcher;
import com.funnelback.publicui.streamedresults.ResultFields;
import com.funnelback.publicui.streamedresults.TransactionToResults;
import com.funnelback.publicui.streamedresults.converters.CSVDataConverter;
import com.funnelback.publicui.streamedresults.converters.JSONDataConverter;
import com.funnelback.publicui.streamedresults.datafetcher.XJPathResultDataFetcher;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;

import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
public class StreamResultsController {
    
    private static final XJPathResultDataFetcher RESULT_DATA_FETCHER = new XJPathResultDataFetcher();

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
    
    /**
     * Gets the data converter to use based on the extension.
     * 
     * @param ext
     * @return
     */
    private DataConverter<Object> getDataConverterFromExtension(String ext) {
        if(ext.equals("json")) {
            return (DataConverter) this.JSONDataConverter;
        }
        
        if(ext.equals("csv")) {
            return (DataConverter) this.CSVDataConverter;
        }
        
        throw new RuntimeException();
        
    }
    /**
     * A special end point that can return all results back to the user regardless of collection size.
     * 
     * <p>This end point will only return the results or fields of the results to the user in
     * the specified format (as set by the extension). The end point allows the user to set
     * the fields of the Results to be returned as well as override the names of those
     * results.<p>
     * 
     * <p>The set of URL options are:</p>
     * <ul>
     * <li>fields: A comma separated list of xPaths used to declare which fields of the Result object should
     * be returned for example "liveUrl,metaData/a" would give the live URL of each reasult as well as the value
     * for metadata 'A'. You can also return the entire Result with "/".</li>
     * <li>fieldnames: Sets the names of the fields that are being requests. For example to rename
     * 'liveURL' to 'URL' and rename metadata 'a' to 'Author', we would set this to 'URL,Author'. If not
     * set the values for fields will be used instead.</li>
     * <li>num_ranks: If not set on the URL num_ranks will be set to the largest possible value, if a smaller amount is
     * required it should be set on the URL, this will ensure that only the requested num_ranks are returned however
     * the query may be run multiple times.</li>
     * <li>start_rank: If 
     * <li>optimisations: A true/false option that lets you turn off optimisations </li>
     * </ul>
     * 
     * <p>By default the liveUrl of every result will be returned to the user. In this mode
     * as num_ranks has not been set in the URL parameters, it will be effectively set to its
     * largest value. By default the CGI parameter fields is 
     * @param request
     * @param response
     * @param fields
     * @param fieldnames
     * @param optimisations
     * @param question
     * @param user
     * @throws Exception
     */
    @RequestMapping("/all-results.*")
    public void acknowledgementCounts(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required=false) CommaSeparatedList fields,
            @RequestParam(required=false) CommaSeparatedList fieldnames,
            @RequestParam(required=false, defaultValue="true") boolean optimisations,
            @Valid SearchQuestion question,
            @ModelAttribute SearchUser user) throws Exception {
        
        ResultFields resultFields = new ResultFields(Optional.of(fields).map(CommaSeparatedList::getList), 
            Optional.of(fieldnames).map(CommaSeparatedList::getList));
        
        if (question.getCollection() != null) {
            SearchQuestionBinder.bind(executionContextHolder.getExecutionContext(), request, question, localeResolver, funnelbackVersion);
            
            
            DataConverter<Object> dataConverter = getDataConverterFromExtension(FilenameUtils.getExtension(request.getRequestURI()));
            
            if(dataConverter == null) {
                log.warn("Search called with an unknown extension '" + request.getRequestURL()+"'.");
                throw new RuntimeException("Unknown extension: " + FilenameUtils.getExtension(request.getRequestURI()));
            }
            
            List<CompiledExpression> compiledExpressions = RESULT_DATA_FETCHER.parseFields(resultFields.getXPaths());
            
            PagedSearcher pageSearcher = new PagedSearcher(question, !optimisations);
            
            try(TransactionToResults transactionToResults = new TransactionToResults(dataConverter, 
                                                                                        compiledExpressions, 
                                                                                        resultFields.getFieldNames(), 
                                                                                        response, 
                                                                                        RESULT_DATA_FETCHER)) {
                pageSearcher.runOnEachPage((q) -> processor.process(q, user), transactionToResults::onEachTransaction);
            }
            
            
            response.getOutputStream().close();
            
        } else {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            response.getOutputStream().close();
        }
    }
}
