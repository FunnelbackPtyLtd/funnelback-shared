package com.funnelback.contentoptimiser.fetchers.impl;

import com.funnelback.common.views.View;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.contentoptimiser.fetchers.InDocCountFetcher;
import com.funnelback.contentoptimiser.utils.PanLook;
import com.funnelback.contentoptimiser.utils.PanLookFactory;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class DefaultInDocCountFetcher implements InDocCountFetcher {

    @Autowired @Setter
    public PanLookFactory panLookFactory;
    
    @Autowired @Setter
    I18n i18n;
    
    @Autowired
    File searchHome;

    @Override
    public Map<String,Integer> getTermWeights(ContentOptimiserModel comparison, String queryWord, String collectionName) {
        Map<String,Integer> termWeights = new HashMap<String,Integer>();
        
        try {
            PanLook lexSearch = panLookFactory.getPanLookForLex(new File(searchHome, DefaultValues.FOLDER_DATA
                            + File.separator + collectionName + File.separator + View.live
                            + File.separator + DefaultValues.FOLDER_IDX + File.separator
                            + DefaultValues.INDEXFILES_PREFIX + ".lex"), queryWord);
            
            for(String line : lexSearch) {
                try{
                    String[] A  = line.split("\\s+");
                    if(A.length == 3) {
                        termWeights.put("_", Integer.parseInt(A[2]));
                    }else if(A.length == 4) {
                        termWeights.put(A[1], Integer.parseInt(A[3]));
                    }else {
                        comparison.getMessages().add(i18n.tr("error.parsingTermWeights"));
                        log.error("Content optimiser was unable to parse line from the lex file: '" + line +"'");
                    }
                } catch (NumberFormatException e) {
                    comparison.getMessages().add(i18n.tr("error.parsingTermWeights"));
                    log.error("Content optimiser was unable to parse line from the lex file: '" + line +"'",e);
                }
            }
            lexSearch.close();
        } catch (IOException e) {
            comparison.getMessages().add(i18n.tr("error.obtainingTermWeights"));
            log.error("IOException when obtaining term weights from Java PanLook",e);
          }          
        return termWeights;
    }

}
