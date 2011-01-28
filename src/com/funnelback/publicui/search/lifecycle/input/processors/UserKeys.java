package com.funnelback.publicui.search.lifecycle.input.processors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.aop.Profiled;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.UserKeysMapper;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Fetches user keys for early binding DLS
 */
@Component("userKeysInputProcessor")
@Log
public class UserKeys implements InputProcessor {

	@Override
	@Profiled
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) throws InputProcessorException {
		if (searchTransaction != null
				&& searchTransaction.getQuestion() != null
				&& searchTransaction.getQuestion().getCollection() != null) {
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
					searchTransaction.getQuestion().getUserKeys().addAll(mapper.getUserKeys(searchTransaction, request));
				} catch (ClassNotFoundException cnfe) {
					throw new InputProcessorException("Invalid security plugin '" + securityPlugin + "'", cnfe);
				}
			}
		}
	}

}
