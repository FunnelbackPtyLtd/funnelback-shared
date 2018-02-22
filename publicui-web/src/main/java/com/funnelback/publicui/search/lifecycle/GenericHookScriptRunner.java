package com.funnelback.publicui.search.lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.extern.log4j.Log4j2;

/**
 * Generic hook script runner that can be used as an {@link InputProcessor},
 * {@link OutputProcessor} or {@link DataFetcher}
 * 
 * @see Collection.Hook
 */
@Log4j2
public class GenericHookScriptRunner implements DataFetcher, InputProcessor, OutputProcessor {

    public static enum Phase {
        Input, Data, Output;
    }
    
    private static final String TYPE_ERROR_MSG = "The entry '%s' with value '%s' in the Map '%s' has an unexpected type. "
            + "It should be either "+String.class.getName()+"[] or "+List.class.getName()+"<"+String.class.getName()+"> "
            + "but was '%s'. "
            + "This value will be removed to prevent a failure at a later stage in the query lifecycle. "
            + "Please check that your hook script only uses supported types when updating this Map.";
    
    /**
     * Type of hook script to run
     */
    private Hook hookScriptToRun;
    
    /**
     * On which phases to run
     */
    private Phase[] phases;
    
    /**
     * @param hookScriptToRun Which hook script to run
     * @param phases At which phase(s) to run
     */
    public GenericHookScriptRunner(Hook hookScriptToRun, Phase... phases) {
        this.hookScriptToRun = hookScriptToRun;
        this.phases = phases;
    }
    
    public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
        if (ArrayUtils.contains(phases, Phase.Data)) {
            runHookScript(searchTransaction);
        }
    }
    
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (ArrayUtils.contains(phases, Phase.Input)) {
            runHookScript(searchTransaction);
        }
    }

    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (ArrayUtils.contains(phases, Phase.Output)) {
            runHookScript(searchTransaction);
        }
    }
    
    /**
     * Runs the hook script for the collection of the current transaction
     */
    public void runHookScript(SearchTransaction searchTransaction) {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getCollection().getHookScriptsClasses().size() > 0) {
            
            Collection collection = searchTransaction.getQuestion().getCollection();
            Class<Script> hookScriptClass = collection.getHookScriptsClasses().get(hookScriptToRun);
            if (hookScriptClass != null) {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put(Hook.HOOK_NAME, hookScriptToRun);
                    data.put(Hook.SEARCH_TRANSACTION_KEY, searchTransaction);
                    runScript(hookScriptClass, data);
                    
                    fixMapsWithArrayLists(searchTransaction);
                } catch (Throwable t) {
                    String msg = "Error while running '" + hookScriptToRun.toString() + "' hook for collection '" + collection.getId() + "'";
                    SearchQuestionType searchQuestionType = Optional.ofNullable(searchTransaction).map(s -> s.getQuestion()).map(q -> q.getQuestionType()).orElse(null);
                    
                    if(searchQuestionType != null) {
                        msg += " on a search of type '" + searchQuestionType + "'";
                    }
                    if(searchTransaction.getExtraSearchName().isPresent()) {
                        msg += " in extra search '" + searchTransaction.getExtraSearchName().get() + "'.";
                        if(searchQuestionType == SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH) {
                            msg +=  " To see this extra search in the JSON/XML output disable the config option: '" 
                                + FrontEndKeys.ModernUI.REMOVE_INTERNAL_EXTRA_SEARCHES.getKey() + "'";
                        }
                    }
                    
                    
                    
                    log.error(msg, t);
                }
            }
        }
    }
    
    /**
     * Runs a script
     * @param scriptClass Class of the script to run
     * @param data Data to pass to the script
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static Object runScript(Class<Script> scriptClass, Map<String, ?> data) throws InstantiationException, IllegalAccessException {
        Script s = scriptClass.newInstance();
        Binding binding = new Binding();
        for (Map.Entry<String, ?> entry: data.entrySet()) {
            binding.setVariable(entry.getKey(), entry.getValue());
        }
        s.setBinding(binding);
        return s.run();
    }

    
    /**
     * <p>Problem: Groovy arrays are internally implemented as {@link ArrayList}s. It means that when
     * someone do something like <code>transaction.question.rawInputParameters["key"] = ["value1", "value2"]</code>
     * the <code>rawInputParameters</code> map now contains a value which is of type <code>ArrayList</code>
     * instead of <code>String[]</code>.</p>
     * 
     * <p>That causes run-time errors when the Map is read by code expecting a <code>String[]</code>
     * 
     * <p>This is possible even if the {@link Map} is declared as <code>&lt;String, String[]&gt;</code>
     * because of the way generics are implemented in Java (type erasure). There's no run-time guarantee
     * that prevents anyone from putting arbitrary objects types in the map.</p>
     * 
     * <p>So we will iterate over the map, check for {@link ArrayList}s and replace them with String arrays.</p>
     * 
     * <p>That sucks :(</p>
     * 
     * @since v11.6
     */
    private void fixMapsWithArrayLists(SearchTransaction transaction) {
        if(transaction.getQuestion() != null) {
            fixMapWithArrayLists(transaction.getQuestion().getAdditionalParameters(), "additionalParameters");
            fixMapWithArrayLists(transaction.getQuestion().getRawInputParameters(), "rawInputParameters");
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void fixMapWithArrayLists(Map<String, String[]> map, String mapName) {
        // Can't remove while iterating otherwise we get a ConcurrentModificationException
        Set<String> keysToRemove = new HashSet<String>();
        
        for(String key: map.keySet()) {
            Object value = map.get(key);
            if (value instanceof List) {
                try {
                    // Convert to String[]
                    map.put(key, (String[]) ((List) value).toArray(new String[0]));
                    log.debug("Converted entry '"+key+"' from List to String[]");
                } catch (Exception e) {
                    log.warn(String.format(TYPE_ERROR_MSG, key, value, mapName, value.getClass().getName()));
                    keysToRemove.add(key);
                }
            } else if (! (value instanceof String[])) {
                log.warn(String.format(TYPE_ERROR_MSG, key, value, mapName, value.getClass().getName()));
                keysToRemove.add(key);
            }
        }
        
        for(String key: keysToRemove) {
            map.remove(key);
        }
    }
    
    @Override
    public String getId() {
        return this.getClass().getSimpleName()+" "+hookScriptToRun;
    }

}
