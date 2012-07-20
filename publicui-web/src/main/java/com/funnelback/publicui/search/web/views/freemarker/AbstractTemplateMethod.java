package com.funnelback.publicui.search.web.views.freemarker;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.i18n.I18n;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * <p>Convenience template to implement a FreeMarker method.</p>
 * 
 * <p>It does the sanity checks for valid arguments.</p>
 *
 */
@RequiredArgsConstructor
public abstract class AbstractTemplateMethod implements TemplateMethodModelEx {

	@Autowired
	@Setter protected I18n i18n;
	
	/** Number of required arguments */
	private final int requiredArgumentsCount;
	/** Number of optional arguments, excluding required ones */
	private final int maxOptionalArgumentsCount;
	/** Whether arguments can have a null value or not */
	@Getter private final boolean nullArgsPermitted;
	
	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		if (arguments == null && requiredArgumentsCount != 0) {
			throw new TemplateModelException(
					i18n.tr("freemarker.method.arguments.null",
							requiredArgumentsCount,
							StringUtils.join(getRequiredArgumentsNames(), ",")));
		} else if (arguments != null && arguments.size() != requiredArgumentsCount
				&& ! (arguments.size() > requiredArgumentsCount
						&& arguments.size() <= requiredArgumentsCount+maxOptionalArgumentsCount)) {
			
			// Missing arguments. Does this method supports optional arguments ?
			if (maxOptionalArgumentsCount > 0) {
				throw new TemplateModelException(
						i18n.tr("freemarker.method.arguments.missing.optional",
								requiredArgumentsCount,
								StringUtils.join(getRequiredArgumentsNames(), ","),
								maxOptionalArgumentsCount,
								StringUtils.join(getOptionalArgumentsNames(), ",")));
			} else{
				throw new TemplateModelException(
					i18n.tr("freemarker.method.arguments.missing",
							requiredArgumentsCount,
							StringUtils.join(getRequiredArgumentsNames(), ",")));
			}
		} else {
			if (! nullArgsPermitted) {
				// Check that mandatory arguments are not null
				for (int i=0; i<requiredArgumentsCount; i++) {
					if (arguments.get(i) == null) {
						throw new TemplateModelException(
								i18n.tr("freemarker.method.arguments.value.null",
										Integer.toString(i+1),
										"freemarker.method."+this.getClass().getSimpleName()+".req."+(i+1)));
					}
				}
			}
		}
		
		try {
			return execMethod(arguments);
		} catch (ClassCastException cce) {
			// Try to detect errors due to users passing invalid parameters
			// types, such as a string instead of a number, etc.
			// In this case, throw the exception with a nice message so that it
			// appears on the form file.
			if (cce.getMessage().contains("freemarker")) {
				throw new TemplateModelException(i18n.tr("freemarker.method.classcast"), cce);
			} else {
				throw cce;
			}
		}
	}

	private String[] getRequiredArgumentsNames() {
		String[] names = new String[requiredArgumentsCount];
		for (int i=0; i<requiredArgumentsCount; i++) {
			names[i] = i18n.tr("freemarker.method."+this.getClass().getSimpleName()+".req."+(i+1));
		}
		return names;
	}
	
	private String[] getOptionalArgumentsNames() {
		String[] names = new String[maxOptionalArgumentsCount];
		for (int i=0; i<maxOptionalArgumentsCount; i++) {
			names[i] = i18n.tr("freemarker.method."+this.getClass().getSimpleName()+".opt."+(i+1));
		}
		return names;
	}
	
	/**
	 * <p>Actual execution of the method once the arguments
	 * have been checked</p>
	 * @param arguments
	 * @return
	 */
	protected abstract Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException;
	
}
