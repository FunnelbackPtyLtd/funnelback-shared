package com.funnelback.publicui.search.service.suggest;

import com.funnelback.common.View;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.suggest.SuggestQuery.Sort;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.Suggester;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Suggester that uses the <code>libqs</code> shared library to get
 * suggestions.
 * 
 * @since v12.0
 */
@Component
public class LibQSSuggester implements Suggester {
    
    @Override
    public List<Suggestion> suggest(Collection c, String profileId, String partialQuery,
        int numSuggestions, Sort sort, double alpha, String category) {

        File indexStem = new File(c.getConfiguration().getCollectionRoot()
            + File.separator + View.live
            + File.separator + DefaultValues.FOLDER_IDX,
            DefaultValues.INDEXFILES_PREFIX);                

        return new PadreConnector(indexStem)
            .suggest(partialQuery)
            .suggestionCount(numSuggestions)
            .alpha(alpha)
            .forCategory(category)
            .forProfile(profileId)
            .sortBy(sort)
            .fetch();
        
    }

}
