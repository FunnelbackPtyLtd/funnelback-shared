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
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.streamedresults.CommaSeparatedList;
import com.funnelback.publicui.streamedresults.CommaSeparatedListEditor;
import com.funnelback.publicui.streamedresults.DataConverter;
import com.funnelback.publicui.streamedresults.PagedQuery;
import com.funnelback.publicui.streamedresults.ResultFields;
import com.funnelback.publicui.streamedresults.TransactionToResults;
import com.funnelback.publicui.streamedresults.converters.CSVDataConverter;
import com.funnelback.publicui.streamedresults.converters.JSONDataConverter;
import com.funnelback.publicui.streamedresults.converters.JSONPDataConverter;
import com.funnelback.publicui.streamedresults.datafetcher.XJPathResultDataFetcher;
import com.funnelback.publicui.utils.JsonPCallbackParam;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.springmvc.web.binder.GenericEditor;

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
        binder.registerCustomEditor(JsonPCallbackParam.class, new GenericEditor(JsonPCallbackParam::new));
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
    private DataConverter<Object> getDataConverterFromExtension(String ext, Optional<JsonPCallbackParam> callback) {
        if(ext.equals("json")) {
            if(callback.isPresent()) {
                return (DataConverter) new JSONPDataConverter(callback.get(), this.JSONDataConverter);
            }
            return (DataConverter) this.JSONDataConverter;
        }
        
        if(ext.equals("csv")) {
            return (DataConverter) this.CSVDataConverter;
        }
        
        return null;
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
     * attempt to turn off ranking and result processing options (e.g. metadata/facet counting), as well as reducing the
     * size of the result summaries returned (SBL=1) and number of bytes of metadata returned (MBL=1).
     * It is not recommended
     * to turn this on and instead you should enable each option to be used by setting it in the request URL.</li>
     * </ul>
     * 
     * <p>By default the liveUrl of every result will be returned to the user.</p>
     * 
     * <p>The available extensions are 'csv' and 'json'</p>
     * 
     * <p>The comma separated lists are encoded like a record in a CSV file following RFC 4180</p>
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
    public void getAllResults(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required=false) CommaSeparatedList fields,
            @RequestParam(required=false) CommaSeparatedList fieldnames,
            @RequestParam(required=false, defaultValue="true") boolean optimisations,
            @RequestParam(required=false) String fileName,
            @Valid SearchQuestion question,
            @ModelAttribute SearchUser user,
            @RequestParam(required=false) JsonPCallbackParam callback) throws Exception {
        
        getAllResults(request, response, fields, fieldnames, fileName, optimisations, question, user, SearchQuestionType.SEARCH_GET_ALL_RESULTS,
            callback);
        
    }
    
    public void getAllResults(
        HttpServletRequest request,
        HttpServletResponse response,
        CommaSeparatedList fields,
        CommaSeparatedList fieldnames,
        String fileName,
        boolean optimisations,
        @Valid SearchQuestion question,
        @ModelAttribute SearchUser user,
        SearchQuestionType searchQuestionType,
        JsonPCallbackParam callback) throws Exception {
        
        // Parse the fields and fieldnames, this ensures that the size of the resulting lists are the same.
        ResultFields resultFields = new ResultFields(Optional.ofNullable(fields).map(CommaSeparatedList::getList), 
            Optional.ofNullable(fieldnames).map(CommaSeparatedList::getList));
        
        if (question.getCollection() != null) {
            question.setQuestionType(searchQuestionType);
            SearchQuestionBinder.bind(executionContextHolder.getExecutionContext(), request, question, localeResolver, funnelbackVersion);
            
            // Find the data converter to use based on the extension.
            DataConverter<Object> dataConverter = getDataConverterFromExtension(
                FilenameUtils.getExtension(request.getRequestURI()),
                Optional.ofNullable(callback));
            
            // TODO I am not sure how an exception should be returned here. Do I try to make a message in the 
            // requested format e.g. csv/json or should I write nothing or should I just write some text back
            // which is what the JAVA URL connection seems to expect (ie that error case returned bytes are different from
            // the non error case bytes).
            if(dataConverter == null) {
                log.debug("Search called with an unknown extension '" + request.getRequestURL()+"'.");
                response.sendError(HttpStatus.SC_BAD_REQUEST, "Unknown extension, valid extensions are '.json' and '.csv'.");
                return;
            }
            
            // Compile the xPaths once.
            List<CompiledExpression> compiledExpressions;
            try {
                compiledExpressions = RESULT_DATA_FETCHER.parseFields(resultFields.getXPaths());
            } catch (Exception e) {
                log.debug("Unable to parse the xPath fields.", e);
                response.sendError(HttpStatus.SC_BAD_REQUEST, "Unable to parse the xPath fields: " + e.getMessage());
                return;
            }

            // Set the result file name if provided.
            addContentDispositionHeader(response, fileName);

            // Now execute our query using the Paged searcher which takes care of making smaller request
            // then pass the result of each search the TransactionToResults class which will convert
            // the SearchTransaction to the data type expected e.g. CSV.
            try {
                PagedQuery pageSearcher = new PagedQuery(question, !optimisations);
                try(TransactionToResults transactionToResults = new TransactionToResults(dataConverter, 
                                                                                            compiledExpressions, 
                                                                                            resultFields.getFieldNames(), 
                                                                                            response, 
                                                                                            RESULT_DATA_FETCHER)) {
                    pageSearcher.runOnEachPage((q) -> processor.process(q, user, Optional.empty(), Optional.empty()), transactionToResults::onEachTransaction);
                }
            } catch (Exception e) {
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }

            response.getOutputStream().close();
            
        } else {
            response.sendError(HttpStatus.SC_NOT_FOUND, "Collection not found.");
            response.getOutputStream().close();
        }
        
    }

    /**
     * Set the 'Content-Disposition' header on a given response. 
     * 
     * @see https://jira.squiz.net/browse/FUN-12913
     * @param response - Response object whose header to set
     * @param fileName - File name, e,g 'cats.csv'
     */
    public void addContentDispositionHeader(HttpServletResponse response, String fileName) {
        if(fileName != null && !fileName.isEmpty()) {
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        }
    }
}
