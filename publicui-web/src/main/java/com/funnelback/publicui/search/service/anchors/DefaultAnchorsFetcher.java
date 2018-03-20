package com.funnelback.publicui.search.service.anchors;

import com.funnelback.common.views.View;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.lock.ThreadSharedFileLock.FileLockException;
import com.funnelback.common.padre.SdinfoFile;
import com.funnelback.common.padre.SdinfoFile.SdinfoEntry;
import com.funnelback.contentoptimiser.utils.PanLook;
import com.funnelback.contentoptimiser.utils.PanLookFactory;
import com.funnelback.dataapi.connector.padre.PadreConnector;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoResult;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorDetail;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.index.QueryReadLock;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.exec.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Component
public class DefaultAnchorsFetcher implements AnchorsFetcher {
    
    @Autowired
    File searchHome;

    @Autowired 
    public PanLookFactory panLookFactory;
    
    @Autowired @Setter
    I18n i18n;
    
    @Autowired
    @Setter @Getter
    protected QueryReadLock queryReadLock;

    @Override
    public AnchorModel fetchGeneral(String indexUrl, int docNum,String collectionName, Collection col) {
        AnchorModel model = new AnchorModel();
        Map<String,AnchorDescription> anchors = commonAnchorsProcessing(indexUrl, docNum, collectionName, model, col);
        
        List<AnchorDescription> anchorsList = new ArrayList<AnchorDescription>(anchors.values());
        Collections.sort(anchorsList);
        model.setAnchors(anchorsList);
        
        return model;
    }
    
    @Override
    public AnchorModel fetchDetail(String indexUrl, int docNum, String collectionName,
            String anchortext,int start, Collection col) {
        AnchorModel model = new AnchorModel();
        Map<String,AnchorDescription> anchors = commonAnchorsProcessing(indexUrl, docNum, collectionName, model, col);

        String lookupAnchortext = AnchorDescription.cleanAnchorText(anchortext);
        AnchorDetail detail = new AnchorDetail(lookupAnchortext);
        if(anchors.containsKey(lookupAnchortext)) {
        
            AnchorDescription description = anchors.get(lookupAnchortext);
            
            File indexStem = new File(searchHome, DefaultValues.FOLDER_DATA + File.separator + collectionName
                            + File.separator + View.live + File.separator + DefaultValues.FOLDER_IDX
                            + File.separator + DefaultValues.INDEXFILES_PREFIX);
            
            List<String> sortedList = new ArrayList<String>(description.getLinksTo());
            Collections.sort(sortedList);
            
            detail.setSize(sortedList.size());
            detail.setStart(start+1);
            int i = 0;
            for(String linkingDocNum : sortedList){
                if(i >= start) detail.getUrls().add(getUrlFromDocnum(model, linkingDocNum, indexStem));
                i++;
                
                if(i > start + AnchorDetail.MAX_URLS_PER_PAGE) {
                    break;
                }
            }
            detail.setEnd(i);

        }
        model.setDetail(detail);    
        
        return model;
    }

    private Map<String,AnchorDescription> commonAnchorsProcessing(String indexUrl, int docNum, String collectionName,
            AnchorModel model, Collection col) {
        model.setCollection(collectionName);
        String formattedDocnum = String.format("%08d",docNum);
        model.setDocNum(formattedDocnum);
    
    // Take the query read lock to prevent updates/commits from changing the index while we read it.
    try {
        queryReadLock.lock(col);
    } catch (FileLockException fle) {
        log.warn("Could not Query Read Lock: " + col.getId(), fle);
        return new HashMap<>();
    }

    try {

        File indexStem = new File(searchHome, DefaultValues.FOLDER_DATA + File.separator + collectionName
                        + File.separator + View.live + File.separator + DefaultValues.FOLDER_IDX
                        + File.separator + DefaultValues.INDEXFILES_PREFIX);

    //If using push2 collection, try to find the correct generation containing the index url.  
    switch (col.getType()) {
        case push2:
            if (indexUrl != null) {
                for(SdinfoEntry entry : SdinfoFile.readSdinfoFile(new File(indexStem.getParentFile().getAbsolutePath() + File.separatorChar + Files.Index.SDINFO))) {
                    try {
                        File generationStem = new File (entry.getIndexPath());
                        DocInfoResult result = new PadreConnector(searchHome, generationStem).docInfo(new URI(indexUrl)).fetch();
                        //Erroneous/missing results are not returned here, so if we get a result, it's the right one. 
                        if (result.asList().size() > 0) {
                            indexStem = generationStem;
                            break;
                        }
                    } catch (URISyntaxException use) {
                        throw new RuntimeException(use);
                    }
                }
            }
            //indexUrl was not found inside collection
            return new HashMap<>(); 

        default:
            break;
    }
        
        File distilledFile = new File(indexStem.getAbsolutePath() + ".distilled");
        model.setDistilledFileName(distilledFile.toString()); 
        
        PanLook panLookOutput = callPanLook(model, formattedDocnum, distilledFile);
        model.setUrl(getUrlFromDocnum(model,formattedDocnum,indexStem));
        if(panLookOutput != null) {
            Map<String,AnchorDescription> ret = parseAnchorsToMap(model, panLookOutput);
            try {
                panLookOutput.close();
            } catch (IOException e) {
                
            }
            return ret;
        } else {
            return new HashMap<>();
        }

        } catch (InterruptedException | FileNotFoundException ie) {
            throw new RuntimeException(ie);
        } finally {
            queryReadLock.release(col);
        }
    }

    private PanLook callPanLook(AnchorModel model, String formattedDocnum,
            File distilledFile) {
        PanLook panLook;
        try {
            panLook = panLookFactory.getPanLook(distilledFile, formattedDocnum);
        } catch (IOException e1) {
            model.setError(i18n.tr("anchors.execute.panlook.io.exception"));
            log.error("I/O exception when calling pan-look: ",e1);
            return null;
        }
        return panLook;
    }
    
    private String getUrlFromDocnum(AnchorModel model, String formattedDocnum,File indexStem) {
        Executor getUrl = new DefaultExecutor();
        File getUrlBinary = new File(searchHome,
            DefaultValues.FOLDER_BIN + File.separator + "get_url_from_docnum" + ((OS.isFamilyWindows()) ? ".exe" : ""));
        CommandLine clAnchors = new CommandLine(getUrlBinary);
        
        clAnchors.addArgument(indexStem.toString());        
        clAnchors.addArgument(formattedDocnum);

        ByteArrayOutputStream getUrlOutput = new ByteArrayOutputStream();
        getUrl.setStreamHandler(new PumpStreamHandler(getUrlOutput, null));
        try {
            getUrl.execute(clAnchors);
        } catch (ExecuteException e) {
            model.setError(i18n.tr("anchors.execute.geturl.failed"));
            log.error("Execute exception when calling get_url_from_docnum: ", e);
        } catch (IOException e) {
            model.setError(i18n.tr("anchors.execute.geturl.io.exception"));
            log.error("I/O exception when calling get_url_from_docnum: ",e);
        }
        String url = getUrlOutput.toString();
        if(url.indexOf("://") == -1) {
            url = "http://" + url;
        }
        return url;
    }

    public Map<String, AnchorDescription> parseAnchorsToMap(AnchorModel model,
            Iterable<String> anchorsOutput) {
        Pattern p = Pattern.compile("([\\d]+) ([-\\d]+) (.+)");
        Map<String,AnchorDescription> anchors = new HashMap<String,AnchorDescription>();
        for(String line : anchorsOutput) {
            Matcher m = p.matcher(line);
            if(m.matches()) {
                String anchorText = AnchorDescription.cleanAnchorText(m.group(3));
                if(! anchors.containsKey(anchorText)) {
                    anchors.put(anchorText, new AnchorDescription(anchorText));
                }
                anchors.get(anchorText).linkTo(m.group(2));        
                model.setTotalLinks(model.getTotalLinks() +1);
            } else {
                model.setError(i18n.tr("anchors.parse.failed"));
                log.error("Unable to parse a line from the output of '"+DefaultValues.PANLOOK_BINARY+"'. The line was: '"+line+"'.");
                return anchors;
            }
        }
        return anchors;
    }



}
