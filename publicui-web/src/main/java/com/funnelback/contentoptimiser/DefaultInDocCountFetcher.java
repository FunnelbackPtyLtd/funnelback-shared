package com.funnelback.contentoptimiser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.funnelback.utils.PanLook;
import com.funnelback.utils.PanLookFactory;

@Log
@Component
public class DefaultInDocCountFetcher implements InDocCountFetcher {

	@Autowired @Setter
	public PanLookFactory panLookFactory;
	
	@Autowired @Setter
	I18n i18n;
	
	@Override
	public Map<String,Integer> getTermWeights(UrlComparison comparison, String queryWord, Collection collection) {
		Map<String,Integer> termWeights = new HashMap<String,Integer>();
		
		try {
			PanLook lexSearch = panLookFactory.getPanLookForLex(new File(collection.getConfiguration().getCollectionRoot(),DefaultValues.VIEW_LIVE + File.separator + DefaultValues.FOLDER_IDX + File.separator + DefaultValues.INDEXFILES_PREFIX + ".lex"), queryWord);
			
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
		} catch (IOException e) {
			comparison.getMessages().add(i18n.tr("error.obtainingTermWeights"));
			log.error("IOException when obtaining term weights from Java PanLook",e);
  		}  		
		return termWeights;
	}

}
