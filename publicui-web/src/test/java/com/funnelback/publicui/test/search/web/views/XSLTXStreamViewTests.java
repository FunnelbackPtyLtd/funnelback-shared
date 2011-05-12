package com.funnelback.publicui.test.search.web.views;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.search.web.views.XSLTXStreamView;
import com.funnelback.publicui.xml.SearchXStreamMarshaller;

public class XSLTXStreamViewTests {

	private XSLTXStreamView view;
	
	@Before
	public void before() throws Exception {
		view = new XSLTXStreamView(new ClassPathResource("funnelback-legacy.xsl"));
		view.setModelKey(SearchController.ModelAttributes.SearchTransaction.toString());
		SearchXStreamMarshaller marshaller = new SearchXStreamMarshaller();
		marshaller.afterPropertiesSet();	// Required to apply XStream customisation (calls customiseXStream())
		view.setMarshaller(marshaller);
	}
	
	@Test
	public void test() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		String oldXml = FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml"));
		ResultPacket rp = new StaxStreamParser().parse(oldXml);
		
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
		st.getQuestion().setQuery("dummy");
		st.getResponse().setResultPacket(rp);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(SearchController.ModelAttributes.SearchTransaction.toString(), st);
		
		view.render(model, request, response);
		
		String actual = response.getContentAsString();
		Assert.assertNotNull(actual);
		
		// Strip out all that isn't relevant for comparison
		// Spaces at the beginning of the line
		Pattern p = Pattern.compile("^\\s*", Pattern.MULTILINE);
		oldXml = p.matcher(oldXml).replaceAll("");
		
		// line ends
		oldXml = oldXml.replaceAll("\\r?\\n", "\n");		
		actual = actual.replaceAll("\\r?\\n", "\n");
		p = Pattern.compile("\\s+$", Pattern.MULTILINE);
		oldXml = p.matcher(oldXml).replaceAll("");
		
		// Padre encodes "-" in XML
		oldXml = oldXml.replace("&#45;", "-");
		
		// XML header
		oldXml = oldXml.replaceAll("<\\?.*\\?>", "");
		actual = actual.replaceAll("<\\?.*\\?>", "");
		
		// Trailing newline
		oldXml = oldXml.replaceAll("\\n$", "");
		actual = actual.replaceAll("\\n$", "");
		
		// Comments
		oldXml = oldXml.replaceAll("<!--.*?-->\\n?", "");
		
		// Newline between quicklinks tags
		actual = actual.replaceAll("\\n?<(/)?quicklink>\\n?", "<$1quicklink>");
		actual = actual.replaceAll("\\n?<(/)?qltext>\\n?", "<$1qltext>");
		actual = actual.replaceAll("\\n?<(/)?qlurl>\\n?", "<$1qlurl>");
		
		// Newline between some results
		p = Pattern.compile("(</result>).*?(<result>)", Pattern.DOTALL);
		oldXml = p.matcher(oldXml).replaceAll("$1\n$2");
		
		// PADRE date contains space
		p = Pattern.compile("<collection_updated>[.\\n]*(.*?)[\\s]*</collection_updated>", Pattern.DOTALL);
		oldXml = p.matcher(oldXml).replaceAll("<collection_updated>$1</collection_updated>");

		// Tier bars not supported in transformed XML
		p = Pattern.compile("<tier_bar>.*?</tier_bar>", Pattern.DOTALL);
		oldXml = p.matcher(oldXml).replaceAll("");
		
		// No click tracking URL
		actual = actual.replaceAll("<click_tracking_url/>\\n", "");
		
		// Re-order <rmc> equally
		oldXml = reOrder(oldXml, Pattern.compile("<rmc.*</rmc>", Pattern.DOTALL));
		actual = reOrder(actual, Pattern.compile("<rmc.*</rmc>", Pattern.DOTALL));
		
		// Re-order <urlcount> equally
		// FIXME URL COUNT ordering
		oldXml = reOrder(oldXml, Pattern.compile("<urlcount.*</urlcount>", Pattern.DOTALL));
		actual = reOrder(actual, Pattern.compile("<urlcount.*</urlcount>", Pattern.DOTALL));
		
		// <md f... needs to be tested separately because there is no easy way
		// to reorder them
		p = Pattern.compile("<md f[^\\n]*</md>");
		Matcher m = p.matcher(oldXml);
		while (m.find()) {
			Assert.assertTrue(actual.contains(m.group(0)));
		}
		
		// <explain> tags didn't exist prior to v11
		oldXml = oldXml.replaceAll("(?s)<explain>.*?</explain>\\n", "");
		// As well as <cooler_weightings>
		oldXml = oldXml.replaceAll("(?s)<cooler_weightings>.*?</cooler_weightings>\\n", "");
		
		// Remove <unexpected_tag> used for other tests
		oldXml = oldXml.replaceAll("(?s)<unexpected_tag>.*?</unexpected_tag>\\n", "");
		
		
		
		// Then strip the <md> tags
		oldXml = oldXml.replaceAll("<md f[^\\n]*</md>", "");
		actual = actual.replaceAll("<md f[^\\n]*</md>", "");
		
		Assert.assertEquals(oldXml, actual);
	}
	
	/**
	 * Sort the lines matching a given Pattern
	 * @param question
	 * @param p
	 * @return
	 */
	private String reOrder(String input, Pattern p) {
		Matcher m = p.matcher(input);
		if (m.find()) {
			String rmcs[] = m.group(0).split("\\n");
			Arrays.sort(rmcs);
			return m.replaceAll(StringUtils.join(rmcs, "\n"));
		}
		return input;
	}
}
