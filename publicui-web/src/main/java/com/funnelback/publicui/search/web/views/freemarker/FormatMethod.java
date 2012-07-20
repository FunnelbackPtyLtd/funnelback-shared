package com.funnelback.publicui.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.utility.DeepUnwrap;

public class FormatMethod extends AbstractTemplateMethod {

	public static final String NAME = "format";
	
	public FormatMethod() {
		super(2, 3, false);
	}

	@Override
	protected Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		Locale l = (Locale) ((BeanModel) arguments.get(0)).getWrappedObject();
		String str = ((TemplateScalarModel) arguments.get(1)).getAsString();
		
		if (arguments.size() > 2) {
			if (arguments.get(2) instanceof SimpleSequence) {
				// Unwrap arguments
				List<Object> args = new ArrayList<Object>();
				SimpleSequence seq = (SimpleSequence) arguments.get(2);
				for (int i=0; i<seq.size(); i++) {
					args.add(DeepUnwrap.permissiveUnwrap((TemplateModel) seq.get(i)));
				}
				return String.format(l, str, args.toArray());
			} else {
				// Single argument
				return String.format(l, str, DeepUnwrap.permissiveUnwrap((TemplateModel) arguments.get(2)));
			}
		} else {
			return String.format(l, str);
		}
	}

}
