package com.funnelback.publicui.web.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.TraxSource;

/**
 * {@link View} that serialize the model using XStream, then
 * transforms it using a given XSL stylesheet.
 * 
 * Used mainly to provide a compatible legacy XML format to mimic
 * the Perl UI xml.cgi
 */
@Log
public class XSLTXStreamView extends AbstractView {

	public static final String CONTENT_TYPE_LEGACY = "text/legacy-xml";
	
	/**
	 * Key to serialize in the model
	 */
	@Setter private String modelKey;
	
	/**
	 * Pre-compiled XSL stylesheet
	 */
	private Templates templates;
	
	public XSLTXStreamView(Resource xslt) throws Exception {
		log.debug("Compiling XSL from '" + xslt.toString() + "'");
		templates = TransformerFactory.newInstance().newTemplates(new StreamSource(xslt.getInputStream()));
	}
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/xml;charset=UTF-8");
		TraxSource source = new TraxSource(model.get(modelKey), new XStream());
		templates.newTransformer().transform(source, new StreamResult(response.getOutputStream()));
	}
	
	@Override
	public String getContentType() {
		return CONTENT_TYPE_LEGACY;
	}

}
