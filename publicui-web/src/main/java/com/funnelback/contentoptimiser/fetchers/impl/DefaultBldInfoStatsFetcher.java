package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.indexer.BuildInfoUtils;
import com.funnelback.contentoptimiser.fetchers.BldInfoStatsFetcher;
import com.funnelback.contentoptimiser.processors.impl.BldInfoStats;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.service.IndexRepository;

/**
 * This class reads the bldinfo for information relevant to the content optimiser, and returns a BldInfoStats object containing that info. 
 * 
 * @author tim
 *
 */
@Log4j
@Component
@ToString
public class DefaultBldInfoStatsFetcher implements BldInfoStatsFetcher {
    
    @Autowired @Setter
    I18n i18n;
    
    @Autowired @Setter
    IndexRepository indexRepository;
    
    public BldInfoStats fetch(ContentOptimiserModel model, Collection collection) throws IOException {
        String[] bldInfos = collection.getMetaComponents();
        long totalDocs = 0;
        BigDecimal totalWordsInDocs = new BigDecimal(0);

        if(bldInfos.length == 0) {
            totalDocs = Long.parseLong(indexRepository.getBuildInfoValue(collection.getId(), BuildInfoUtils.BuildInfoKeys.Num_docs.toString()));
            String avgDocs = indexRepository.getBuildInfoValue(collection.getId(), BuildInfoUtils.BuildInfoKeys.Average_document_length.toString());
            if(avgDocs.contains(" ")) avgDocs = avgDocs.substring(0, avgDocs.indexOf(' '));
            totalWordsInDocs = totalWordsInDocs.add(
                    new BigDecimal(Long.parseLong(indexRepository.getBuildInfoValue(collection.getId(), BuildInfoUtils.BuildInfoKeys.Num_docs.toString())))
                    .multiply(new BigDecimal(avgDocs)));
        } else {
            for(String coll : bldInfos) {
                totalDocs += Long.parseLong(indexRepository.getBuildInfoValue(coll, BuildInfoUtils.BuildInfoKeys.Num_docs.toString()));
                String avgDocs = indexRepository.getBuildInfoValue(coll, BuildInfoUtils.BuildInfoKeys.Average_document_length.toString());
                if(avgDocs.contains(" ")) avgDocs = avgDocs.substring(0, avgDocs.indexOf(' '));
                totalWordsInDocs = totalWordsInDocs.add(
                        new BigDecimal(Long.parseLong(indexRepository.getBuildInfoValue(coll, BuildInfoUtils.BuildInfoKeys.Num_docs.toString())))
                        .multiply(new BigDecimal(avgDocs)));
            }            
        }
        int avgWordsInDoc = 0;
        
        if(totalDocs != 0) {
          avgWordsInDoc = totalWordsInDocs.divide(new BigDecimal(totalDocs),RoundingMode.HALF_DOWN).intValue();
        } else {
            log.error("Didn't find any documents reported in the bldinfo files. \".bldinfo\"s were: " + Arrays.toString(bldInfos));
            model.getMessages().add(i18n.tr("error.readingBldinfo"));
        }
        return new BldInfoStats(totalDocs,avgWordsInDoc); 
    }
}
