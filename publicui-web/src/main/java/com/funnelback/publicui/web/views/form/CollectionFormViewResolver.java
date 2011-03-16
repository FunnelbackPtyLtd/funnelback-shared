package com.funnelback.publicui.web.views.form;

import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

import net.sf.ehcache.hibernate.management.impl.BeanUtils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

public class CollectionFormViewResolver extends AbstractCachingViewResolver implements Ordered {

	private static final String SEPARATOR = ":";
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Setter private Class<? extends AbstractCollectionFormView> viewClass;
	
	@Getter @Setter int order;
	
	@Override
	protected AbstractCollectionFormView loadView(String viewName, Locale locale) throws Exception {
		if (viewName == null || viewName.indexOf(SEPARATOR) < 0) {
			// Let the next view resolver handle it
			return null;
		}
		
		String collectionAndName[] = viewName.split(SEPARATOR);
		Collection c = configRepository.getCollection(collectionAndName[0]);
		String formName = collectionAndName[1];
		if (c == null || c.getForms().get(formName) == null) {
			// Let the next view resolver handle it
			return null;
		}

		AbstractCollectionFormView view = org.springframework.beans.BeanUtils.instantiate(viewClass);
		getApplicationContext().getAutowireCapableBeanFactory().autowireBean(view);
		view.setTemplateContent(c.getForms().get(formName));
		view.setCollectionId(c.getId());
		
		return view;
	}
	
}
