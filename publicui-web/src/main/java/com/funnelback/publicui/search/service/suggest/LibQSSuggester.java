package com.funnelback.publicui.search.service.suggest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.Suggestion;
import com.funnelback.publicui.search.model.transaction.Suggestion.ActionType;
import com.funnelback.publicui.search.model.transaction.Suggestion.DisplayType;
import com.funnelback.publicui.search.service.Suggester;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Suggester that uses the <code>libqs</code> shared library to get
 * suggestions.
 * 
 * @since v12.0
 */
@Component
public class LibQSSuggester implements Suggester {
    
    public interface PadreQS extends Library {
        public PadreQS INSTANCE = (PadreQS) Native.loadLibrary("qs", PadreQS.class);
        
        NativeSuggestion generate_suggestions(String stem, String profile, int numToShow, int sortCode, String partialQuery);
        void free_suggestion_array(Pointer p);
        void lqs_set_debug(int level);
    }
    
    @Override
    public List<Suggestion> suggest(Collection c, String profileId, String partialQuery, int numSuggestions, Sort sort) {
        
        File indexStem = new File(c.getConfiguration().getCollectionRoot()
                + File.separator + DefaultValues.VIEW_LIVE
                + File.separator + DefaultValues.FOLDER_IDX,
                DefaultValues.INDEXFILES_PREFIX);                
        
        List<Suggestion> suggestions = new ArrayList<Suggestion>();
        
        NativeSuggestion ns = null;
        try {
            ns = PadreQS.INSTANCE.generate_suggestions(
                    indexStem.getAbsolutePath(),
                    profileId,
                    numSuggestions,
                    sort.value,
                    partialQuery);
            
            if (ns != null) {
                NativeSuggestion[] nss = (NativeSuggestion[]) ns.toArray(numSuggestions);
                // LibQS returns an array with 'numSuggestions' slots. If there are less than
                // numSuggestions the key will be the empty string
                for (int i=0; i<nss.length && !"".equals(nss[i].key); i++) {
                    suggestions.add(nss[i].toSuggestion());
                }
                
                
                
            }
        } finally {
            if (ns != null && ns.getPointer() != null) {
                PadreQS.INSTANCE.free_suggestion_array(ns.getPointer());
            }
        }
        
        return suggestions;
    }
    
    /** A native suggestion as returned by <code>libqs</code> */
    public static class NativeSuggestion extends Structure {
        public String length;
        public String key;
        public String weight;
        public String display;
        public String displayType;
        public String category;
        public String categoryType;
        public String action;
        public String actionType;
        
        public Suggestion toSuggestion() {
            Suggestion s = new Suggestion();
            if (!"".equals(length)) {
                s.setLength(Integer.parseInt(length));
            }
            s.setKey(key);
            if (!"".equals(weight)) {
                s.setWeight(Float.parseFloat(weight));
            }
            s.setDisplay(display);
            s.setDisplayType(DisplayType.fromValue(displayType));
            s.setCategory(category);
            s.setCategoryType(categoryType);
            s.setAction(action);
            s.setActionType(ActionType.fromValue(actionType));
            
            return s;
        }
        
        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[] {
                "length",
                "key",
                "weight",
                "display",
                "displayType",
                "category",
                "categoryType",
                "action",
                "actionType"
            });
        }
    }

}
