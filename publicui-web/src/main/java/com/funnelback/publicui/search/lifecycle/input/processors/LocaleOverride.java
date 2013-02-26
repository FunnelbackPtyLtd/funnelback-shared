package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Locale;

import lombok.extern.log4j.Log4j;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>Overrides the automatically detected locale with a CGI parameter.</p>
 * 
 * <p>One parameter will override only the UI locale, whereas a second one
 * will override both the UI and the query processor (It's the same <tt>lang</tt>
 * parameter as PADRE).</p>
 * 
 * @since 12.0
 */
@Log4j
@Component("localeOverrideInputProcessor")
public class LocaleOverride extends AbstractInputProcessor {

	private static final String SEPARATOR = "_";
	
	@Override
	public void processInput(SearchTransaction searchTransaction)
			throws InputProcessorException {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
			String lang = searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.LANG_UI);
			
			if (lang != null && !"".equals(lang)) {
				searchTransaction.getQuestion().setLocale(getLocale(lang));
				log.debug("UI locale set to '"+searchTransaction.getQuestion().getLocale()+"' via CGI override (&"+RequestParameters.LANG_UI+"="+lang+")");
			} else {
				lang = searchTransaction.getQuestion().getInputParameterMap().get(RequestParameters.LANG);
				if (lang != null && !"".equals(lang)) {
					searchTransaction.getQuestion().setLocale(getLocale(lang));
					log.debug("UI and query processor locale set to '"+searchTransaction.getQuestion().getLocale()+"' via CGI override (&"+RequestParameters.LANG+"="+lang+")");					
				} else {
					log.debug("Locale auto-detected to '"+searchTransaction.getQuestion().getLocale()+"'");
				}		
			}
		}

	}
	
	private Locale getLocale(String lang) {
		if (lang.contains(SEPARATOR)) {
			String[] code = lang.split(SEPARATOR);
			return new Locale(code[0], code[1]);
		} else {
			return new Locale(lang);
		}
	}

}
