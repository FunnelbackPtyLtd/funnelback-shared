package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Date;
import java.util.TimeZone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.function.CallableCE;
import com.funnelback.common.lock.QueryReadLock;
import com.funnelback.common.lock.QueryReadLockI;
import com.funnelback.common.padre.index.FollowMetaCollectionProcessorNoCE;
import com.funnelback.common.padre.index.IndexProcessor.MissingIndexException;
import com.funnelback.common.padre.index.ProcessIndex;
import com.funnelback.common.views.View;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.google.common.io.Files;
import com.thoughtworks.xstream.converters.basic.DateConverter;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Gives meta collections the update date of their most recently updated component
 */
@Component("fixMetaCollectionUpdateDateOutputProcessor")
@Log4j2
public class FixMetaCollectionUpdateDate extends AbstractOutputProcessor {

    @Autowired
    private ConfigRepository configRepository;

    @Setter /* public for testing */
    private ProcessIndex processIndex = new ProcessIndex();
    @Setter /* public for testing */
    private QueryReadLockI queryReadLock = new QueryReadLock();

    @Override
    public void processOutput(SearchTransaction searchTransaction) {
        // Ensure we have something to do
        if (SearchTransactionUtils.hasCollection(searchTransaction)
            && SearchTransactionUtils.hasResultPacket(searchTransaction)) {

            Config config = searchTransaction.getQuestion().getCollection().getConfiguration();
            if (config.getCollectionType().equals(Type.meta)) {
                final MutableLong currentUpdateTime = new MutableLong(
                    searchTransaction.getResponse().getResultPacket().getDetails().getCollectionUpdated().getTime());

                queryReadLock.doWithReadLockWithCE(config, (CallableCE<Void, MissingIndexException>) () -> {
                    File indexStem = new File(config.getCollectionRoot(),
                        View.live + File.separator + DefaultValues.FOLDER_IDX + File.separator + DefaultValues.INDEXFILES_PREFIX);

                    processIndex.processIndex(config.getCollectionName(), indexStem.getAbsolutePath(),
                        new FollowMetaCollectionProcessorNoCE<RuntimeException>() {

                            @Override
                            public void atComponentIndex(String collection, String indexStem) {
                                try {
                                    long componentUpdateTime = componentUpdateTime(indexStem);

                                    if (componentUpdateTime > currentUpdateTime.longValue()) {
                                        currentUpdateTime.setValue(componentUpdateTime);
                                    }
                                } catch (FileNotFoundException e) {
                                    log.debug("No index_time file for for meta component - Maybe it has never been updated?" + collection, e);
                                } catch (IOException e) {
                                    log.error("Could not read index time for meta component " + collection, e);
                                } catch (Exception e) {
                                    log.error("Exception getting index time for meta component " + collection, e);
                                }
                            }

                            @Override
                            public void indexStemMissing(String collection, String indexStem) throws MissingIndexException {
                                log.debug("Could not check " + collection + "'s index at " + indexStem
                                    + " for update time because the index stem does not exist. Perhaps it has never been updated.");
                            }
                        });
                    return null;
                });

                searchTransaction.getResponse().getResultPacket().getDetails().setCollectionUpdated(new Date(currentUpdateTime.getValue()));
            }
        }
    }
    
    public /* for testing */ long componentUpdateTime(String indexStem) throws IOException {
        // Using the same type of date converter as SearchXStreamMarshaller for consistency
        // It's not clear if it's thread safe on its own, so I guess I have to assume not
        DateConverter dateConverter = new DateConverter(Details.getUpdateDatePatternWithoutLocal(), new String[] {Details.getUpdateDatePatternWithoutLocal()}, TimeZone.getDefault());
        File componentUpdateTimeFile = new File(new File(indexStem).getParentFile(), "index_time");
        String componentUpdateTime = Files.readFirstLine(componentUpdateTimeFile, StandardCharsets.UTF_8);
        
        Date parsed = (Date) dateConverter.fromString(componentUpdateTime);

        return parsed.getTime();
    }
}
