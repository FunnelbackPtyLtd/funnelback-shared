package com.funnelback.publicui.search.service.index.result;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j;

import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.search.model.padre.Result;

/**
 * Fetches result using the Padre Data Api connector.
 */
@Component
@Log4j
public class DefaultResultFetcher implements ResultFetcher {

    /**
     * Will fetch all existing metadata for a given result
     */
    @Override
    public Result fetchResult(File indexStem, URI resultUri) {
        
        List<DocInfo> dis = new PadreConnector(indexStem)
            .docInfo(resultUri)
            .withMetadata(DocInfoQuery.ALL_METADATA)
            .fetch();

        if (dis.size() < 1) {
            return null;
        } else if (dis.size() > 1) {
            log.warn("More that one result for URL '"+resultUri+"' from index" +
                "'"+indexStem+"'. Returning the first one.");
        }
        
        // Convert DocInfo result into Modern UI Result
        
        DocInfo di = dis.get(0);
        
        Result r = new Result();
        r.setIndexUrl(di.uri.toString());
        r.setDate(di.date);
        r.setFileType(di.fileType);
        r.setFileSize(di.unfilteredLength);
        r.setSummary(di.summaryText);
        r.setTitle(di.title);
        r.getMetaData().putAll(di.metaData);
        
        return r;        
    }

}
