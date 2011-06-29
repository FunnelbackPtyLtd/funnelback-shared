package com.funnelback.publicui.search.service.anchors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorDetail;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;

@Log
@Component
public class DefaultAnchorsFetcher implements AnchorsFetcher {
	
	@Autowired
	File searchHome;

	@Autowired @Setter
	I18n i18n;
	
	@Override
	public AnchorModel fetchGeneral(int docNum,Collection collection) {
		AnchorModel model = new AnchorModel();
		Map<String,AnchorDescription> anchors = commonAnchorsProcessing(docNum, collection, model);
		
		List<AnchorDescription> anchorsList = new ArrayList<AnchorDescription>(anchors.values());
		Collections.sort(anchorsList);
		model.setAnchors(anchorsList);
		
		return model;
	}
	
	@Override
	public AnchorModel fetchDetail(int docNum, Collection collection,
			String anchortext,int start) {
		AnchorModel model = new AnchorModel();
		Map<String,AnchorDescription> anchors = commonAnchorsProcessing(docNum, collection, model);

		String lookupAnchortext = AnchorDescription.cleanAnchorText(anchortext);
		AnchorDetail detail = new AnchorDetail(lookupAnchortext);
		if(anchors.containsKey(lookupAnchortext)) {
		
			AnchorDescription description = anchors.get(lookupAnchortext);
			
			File indexStem = null;
			try {
				indexStem = new File(collection.getConfiguration().getCollectionRoot(), DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX + File.separator + DefaultValues.INDEXFILES_PREFIX);
			} catch (FileNotFoundException e1) {
				model.setError(i18n.tr("anchors.collection.file.not.found"));
				log.error("FileNotFound exception (Collection root doesn't exist): ", e1);
				return model;
			}
			
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

	private Map<String,AnchorDescription> commonAnchorsProcessing(int docNum, Collection collection,
			AnchorModel model) {
		model.setCollection(collection.getId());
		String formattedDocnum = String.format("%08d",docNum);
		model.setDocNum(formattedDocnum);
	
		File indexStem = null;
		try {
			indexStem = new File(collection.getConfiguration().getCollectionRoot(), DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX + File.separator + DefaultValues.INDEXFILES_PREFIX);
		} catch (FileNotFoundException e1) {
			model.setError(i18n.tr("anchors.collection.file.not.found"));
			log.error("FileNotFound exception when obtianing collection root: ", e1);
			return new HashMap<String,AnchorDescription>();
		}
		
		File distilledFile = new File(indexStem.getAbsolutePath() + ".distilled");
		model.setDistilledFileName(distilledFile.toString()); 
		
		ByteArrayOutputStream panLookOutput = new ByteArrayOutputStream(); 
		callPanLook(model, formattedDocnum, distilledFile, panLookOutput);
		model.setUrl(getUrlFromDocnum(model,formattedDocnum,indexStem));
		return parseAnchorsToMap(model, panLookOutput);
	}

	private void callPanLook(AnchorModel model, String formattedDocnum,
			File distilledFile, ByteArrayOutputStream anchorsOutput) {
		Executor getAnchors = new DefaultExecutor();
		File panlook = new File(searchHome, DefaultValues.FOLDER_BIN + File.separator + DefaultValues.PANLOOK_BINARY);
		CommandLine clAnchors = new CommandLine(panlook);
		
		clAnchors.addArgument(formattedDocnum);
		clAnchors.addArgument(distilledFile.toString());
		getAnchors.setStreamHandler(new PumpStreamHandler(anchorsOutput, null));
		try {
			getAnchors.execute(clAnchors);
		} catch (ExecuteException e) {
			model.setError(i18n.tr("anchors.execute.panlook.failed"));
			log.error("Execute exception when calling pan-look: ", e);
			return;
		} catch (IOException e) {
			model.setError(i18n.tr("anchors.execute.panlook.io.exception"));
			log.error("I/O exception when calling pan-look: ",e);
			return;
		}
	}
	
	private String getUrlFromDocnum(AnchorModel model, String formattedDocnum,File indexStem) {
		Executor getUrl = new DefaultExecutor();
		File getUrlBinary = new File(searchHome, DefaultValues.FOLDER_BIN + File.separator + "get_url_from_docnum");
		CommandLine clAnchors = new CommandLine(getUrlBinary);
		
		clAnchors.addArgument(indexStem.toString());		
		clAnchors.addArgument(formattedDocnum);

		ByteArrayOutputStream getUrlOutput = new ByteArrayOutputStream();
		getUrl.setStreamHandler(new PumpStreamHandler(getUrlOutput, null));
		try {
			getUrl.execute(clAnchors);
		} catch (ExecuteException e) {
			model.setError(i18n.tr("anchors.execute.geturl.failed"));
			log.error("Execute exception when calling pan-look: ", e);
		} catch (IOException e) {
			model.setError(i18n.tr("anchors.execute.geturl.io.exception"));
			log.error("I/O exception when calling pan-look: ",e);
		}
		String url = getUrlOutput.toString();
		if(url.indexOf("://") == -1) {
			url = "http://" + url;
		}
		return url;
	}

	public Map<String, AnchorDescription> parseAnchorsToMap(AnchorModel model,
			ByteArrayOutputStream anchorsOutput) {
		String[] A = anchorsOutput.toString().split("\n"); // this is the part to change if this gets too slow - sometimes the output can be very large
		Pattern p = Pattern.compile("^([\\d]+) ([-\\d]+) (.+)$");
		
		Map<String,AnchorDescription> anchors = new HashMap<String,AnchorDescription>();
		
		for(String line : A) {
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
