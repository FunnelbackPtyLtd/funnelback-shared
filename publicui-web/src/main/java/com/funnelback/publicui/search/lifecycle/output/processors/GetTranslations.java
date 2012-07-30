package com.funnelback.publicui.search.lifecycle.output.processors;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Retrieve the i18n bundle containing translated messages
 * for the current locale and collection.
 * 
 * @since 12.0
 */
@Component("getTranslationsOutputProcessor")
public class GetTranslations implements OutputProcessor {

	@Autowired
	@Setter
	private ConfigRepository configRepository;
	
	@Override
	public void processOutput(SearchTransaction searchTransaction)
			throws OutputProcessorException {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)
				&& SearchTransactionUtils.hasCollection(searchTransaction)
				&& searchTransaction.getQuestion().getLocale() != null
				&& SearchTransactionUtils.hasResponse(searchTransaction)
				&& searchTransaction.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.ModernUI.I18N)) {
			searchTransaction.getResponse().getTranslations().putAll(
					configRepository.getTranslations(
							searchTransaction.getQuestion().getCollection().getId(),
							searchTransaction.getQuestion().getProfile(),
							searchTransaction.getQuestion().getLocale()));
		}

	}

}
