package com.funnelback.publicui.search.lifecycle.input.processors;

import groovy.swing.factory.BeanFactory;

import java.util.List;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
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
@CommonsLog
public class UserKeys implements InputProcessor {
	
	@Autowired
	@Setter private I18n i18n;
	
	@Autowired
	@Setter private AutowireCapableBeanFactory beanFactory;
	
	@Override
	public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
		if (SearchTransactionUtils.hasCollection(searchTransaction)) {
			String securityPlugin = searchTransaction.getQuestion().getCollection().getConfiguration().value(
					Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER);
			if (securityPlugin != null && ! "".equals(securityPlugin)) {
				searchTransaction.getQuestion().getUserKeys().addAll(
						getUserKeys(securityPlugin, searchTransaction, i18n, beanFactory));
			}
		}
	}
	
	/**
	 * "Secures" a search transaction by applying the given security plugin
	 * @param securityPlugin Class name of the security plugin ({@link UserKeysMapper})
	 * @param st {@link SearchTransaction} to secure
	 * @param i18n for error messages
	 * @param beanFactory Factory used to create the security plugin instance
	 * @return 
	 * @throws InputProcessorException
	 */
	public static List<String> getUserKeys(String securityPlugin, SearchTransaction st,
			I18n i18n, AutowireCapableBeanFactory beanFactory) throws InputProcessorException {
		log.debug("Will use '" + securityPlugin + "' security plugin");
		
		String className = UserKeysMapper.class.getPackage().getName() + "." + securityPlugin;
		if (securityPlugin.contains(".")) {
			// Use fully qualified class name instead of injecting the package name
			className = securityPlugin;
		}
		
		try {
			Class<?> clazz = Class.forName(className);
			UserKeysMapper mapper = (UserKeysMapper) beanFactory.createBean(clazz);
			return mapper.getUserKeys(st);
		} catch (ClassNotFoundException cnfe) {
			throw new InputProcessorException(i18n.tr("inputprocessor.userkeys.plugin.invalid", securityPlugin), cnfe);
		}
	}

}
