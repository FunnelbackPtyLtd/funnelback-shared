package com.funnelback.publicui.i18n;

import org.springframework.context.i18n.LocaleContextHolder;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Internationalization utils
 */
public class I18n {

	/**
	 * @return  a {@link org.xnap.commons.i18n.I18n} ready for use with the
	 * correct locale.
	 */
	public static org.xnap.commons.i18n.I18n i18n() {
		return I18nFactory.getI18n(I18n.class, "com.funnelback.publicui.search.web.i18n.Messages", LocaleContextHolder.getLocale());
	}
	
}
