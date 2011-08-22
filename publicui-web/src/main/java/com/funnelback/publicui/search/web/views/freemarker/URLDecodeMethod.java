package com.funnelback.publicui.search.web.views.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import lombok.SneakyThrows;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

public class URLDecodeMethod extends AbstractTemplateMethod {

	public static final String NAME = "urlDecode";
	
	public URLDecodeMethod() {
		super(1, 0, false);
	}

	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	protected Object execMethod(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String str = ((TemplateScalarModel) arguments.get(0)).getAsString();
		return URLDecoder.decode(str, System.getProperty("file.encoding"));
	}

}
