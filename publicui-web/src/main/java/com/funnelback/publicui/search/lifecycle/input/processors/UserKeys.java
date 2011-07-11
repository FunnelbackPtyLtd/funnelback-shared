package com.funnelback.publicui.search.lifecycle.input.processors;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.UserKeysMapper;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Fetches user keys for early binding DLS
 */
@Component("userKeysInputProcessor")
@Log
public class UserKeys implements InputProcessor {
	
	@Autowired
	@Setter private I18n i18n;
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasCollection(searchTransaction)) {
			String securityPlugin = searchTransaction.getQuestion().getCollection().getConfiguration().value(
					Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER);
			if (securityPlugin != null && ! "".equals(securityPlugin)) {
				log.debug("Will use '" + securityPlugin + "' security plugin");
				
				String className = UserKeysMapper.class.getPackage().getName() + "." + securityPlugin;
				if (securityPlugin.contains(".")) {
					// Use fully qualified class name instead of injecting the package name
					className = securityPlugin;
				}
				
				try {
					Class<?> clazz = Class.forName(className);
					UserKeysMapper mapper = (UserKeysMapper) BeanUtils.instantiate(clazz);
					searchTransaction.getQuestion().getUserKeys().addAll(mapper.getUserKeys(searchTransaction));
				} catch (ClassNotFoundException cnfe) {
					throw new InputProcessorException(i18n.tr("inputprocessor.userkeys.plugin.invalid", securityPlugin), cnfe);
				}
			}
		}
	}

}
