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
     * <p>Note that the return type is of object this is because I can't seem to do:
     * DataConverter<?> converter = getDataConverterFromExtension("");
     * converter.writeHead(convert.getWritter()); // here is the issue.
     * Java doesn't understand that the type returned by convert.getWritter() is the
     * same type as what is expected by converter.writeHead().</p>
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
     * <p>The set of relevant URL request parameters are:</p>
     * <ul>
     * <li>fields: A comma separated list of xPaths used to declare which fields of the Result object should
     * be returned for example "liveUrl,metaData/a" would give the live URL of each reasult as well as the value
     * for metadata 'A'. You can also return the entire Result with "/".</li>
     * <li>fieldnames: Sets the names of the fields that are being requests. For example to rename
     * 'liveURL' to 'URL' and rename metadata 'a' to 'Author', we would set this to 'URL,Author'. If not
     * set the values for fields will be used instead.</li>
     * <li>num_ranks: If not set on the URL num_ranks will be set to the largest possible value, if a smaller amount is
     * required it should be set on the URL, this will ensure that only the requested num_ranks are returned however
     * the query may be run multiple times. If you are doing client side paging you will need to use the start_rank
     * query processor option to start the next page at the right spot.</li>
     * <li>optimisations: A true/false option that lets you turn off optimisations. In general the optimisations
     * attempt to turn off ranking and result processing options (each metadata/facet counting). It is not recommended
     * to turn this on and instead you should enable each option to be used by setting it in the request URL.</li>
     * </ul>
     * 
     * <p>By default the liveUrl of every result will be returned to the user.</p>
     * 
     * <p>The available extensions are 'csv' and 'json'</p>
     * 
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
        
        // Parse the fields and fieldnames, this ensures that the size of the resulting lists are the same.
        ResultFields resultFields = new ResultFields(Optional.of(fields).map(CommaSeparatedList::getList), 
            Optional.of(fieldnames).map(CommaSeparatedList::getList));
        
        if (question.getCollection() != null) {
            SearchQuestionBinder.bind(executionContextHolder.getExecutionContext(), request, question, localeResolver, funnelbackVersion);
            
            // Find the data converter to use based on the extension.
            DataConverter<Object> dataConverter = getDataConverterFromExtension(FilenameUtils.getExtension(request.getRequestURI()));
            
            // TODO I am not sure how an exception should be returned here. Do I try to make a message in the 
            // requested format e.g. csv/json or should I write nothing or should I just write some text back
            // which is what the JAVA URL connection seems to expect (ie that error case returned bytes are different from
            // the non error case bytes).
            if(dataConverter == null) {
                log.warn("Search called with an unknown extension '" + request.getRequestURL()+"'.");
                throw new RuntimeException("Unknown extension: " + FilenameUtils.getExtension(request.getRequestURI()));
            }
            
            // Compile the xPaths once.
            List<CompiledExpression> compiledExpressions = RESULT_DATA_FETCHER.parseFields(resultFields.getXPaths());
            
            // Now execute our query using the Paged searcher which takes care of making smaller request
            // then pass the result of each search the TransactionToResults class which will convert
            // the SearchTransaction to the data type expected e.g. CSV.
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
