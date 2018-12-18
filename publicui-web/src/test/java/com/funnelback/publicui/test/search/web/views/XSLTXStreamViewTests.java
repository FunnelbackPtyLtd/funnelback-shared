package com.funnelback.publicui.test.search.web.views;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.search.web.views.XSLTXStreamView;
import com.funnelback.publicui.xml.SearchXStreamMarshaller;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class XSLTXStreamViewTests {

    private XSLTXStreamView view;
    
    @Before
    public void before() throws Exception {
        view = new XSLTXStreamView(new ClassPathResource("funnelback-classic.xsl"));
        view.setModelKey(SearchController.ModelAttributes.SearchTransaction.toString());
        SearchXStreamMarshaller marshaller = new SearchXStreamMarshaller();
        marshaller.afterPropertiesSet();    // Required to apply XStream customisation (calls customiseXStream())
        view.setMarshaller(marshaller);
    }
    
    @Test
    public void test() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        byte[] rawBytes = FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/complex.xml"));
        String oldXml = new String(rawBytes, StandardCharsets.UTF_8);
        ResultPacket rp = new StaxStreamParser().parse(new ByteArrayInputStream(rawBytes), StandardCharsets.UTF_8, false);
        
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
        actual = p.matcher(actual).replaceAll("");
        
        // line ends
        oldXml = oldXml.replaceAll("\r?\n", "\n");
        actual = actual.replaceAll("\r?\n", "\n");
        p = Pattern.compile("\\s+$", Pattern.MULTILINE);
        oldXml = p.matcher(oldXml).replaceAll("");
        
        // Padre encodes "-" in XML
        oldXml = oldXml.replace("&#45;", "-");
        
        // XML header
        oldXml = oldXml.replaceAll("<\\?.*\\?>", "");
        actual = actual.replaceAll("<\\?.*\\?>", "");
        
        // Trailing newline
        oldXml = oldXml.replaceAll("\n$", "");
        actual = actual.replaceAll("\n$", "");
        
        // Comments
        oldXml = oldXml.replaceAll("<!--.*?-->\n?", "");
        
        // Newline between quicklinks tags
        actual = actual.replaceAll("\n?<(/)?quicklink>\n?", "<$1quicklink>");
        actual = actual.replaceAll("\n?<(/)?qltext>\n?", "<$1qltext>");
        actual = actual.replaceAll("\n?<(/)?qlurl>\n?", "<$1qlurl>");
        
        // PADRE date contains space
        p = Pattern.compile("<collection_updated>[.\n]*(.*?)[\\s]*</collection_updated>", Pattern.DOTALL);
        oldXml = p.matcher(oldXml).replaceAll("<collection_updated>$1</collection_updated>");

        // Tier bars not supported in transformed XML
        p = Pattern.compile("<tier_bar>.*?</tier_bar>", Pattern.DOTALL);
        oldXml = p.matcher(oldXml).replaceAll("");

        // Newline between some results
        p = Pattern.compile("(</result>)\\s+?(<result>)", Pattern.DOTALL);
        oldXml = p.matcher(oldXml).replaceAll("$1\n$2");

        // Tags not supported in transformed XML
        p = Pattern.compile("<tags>.*?</tags>\r?\n", Pattern.DOTALL);
        oldXml = p.matcher(oldXml).replaceAll("");        
        
        // No click tracking URL
        actual = actual.replaceAll("<click_tracking_url/>\n", "");
        
        // Re-order <rmc> equally
        oldXml = reOrder(oldXml, Pattern.compile("<rmc.*</rmc>", Pattern.DOTALL));
        actual = reOrder(actual, Pattern.compile("<rmc.*</rmc>", Pattern.DOTALL));
        
        // Re-order <urlcount> equally
        // FIXME URL COUNT ordering
        oldXml = reOrder(oldXml, Pattern.compile("<urlcount.*</urlcount>", Pattern.DOTALL));
        actual = reOrder(actual, Pattern.compile("<urlcount.*</urlcount>", Pattern.DOTALL));

        // Remove <collapsed> elements which didn't exist previously and that
        // contains sub <results> that upsets the other subsequent tests
        oldXml = oldXml.replaceAll("(?s)<collapsed.*?>.*?</collapsed>\n", "");

        // Check all the metadata content is present
        p = Pattern.compile("<v>(.*?)</v>");
        Matcher m = p.matcher(oldXml);
        while (m.find()) {
            Assert.assertTrue("Transformed XML should contain '"+m.group(1)+"'", actual.contains(StringEscapeUtils.unescapeXml(m.group(1))));
        }
        // Then strip the <md> tags
        oldXml = oldXml.replaceAll("<md.*?</md>", "");
        actual = actual.replaceAll("<md.*?</md>", "");
        
        // <explain> tags didn't exist prior to v11
        oldXml = oldXml.replaceAll("(?s)<explain>.*?</explain>\n", "");
        // As well as <explain_types>
        oldXml = oldXml.replaceAll("(?s)<explain_types>.*?</explain_types>\n", "");
        // As well as <cooler_weightings>
        oldXml = oldXml.replaceAll("(?s)<cooler_weightings>.*?</cooler_weightings>\n", "");
        // As well as <cooler_names>
        oldXml = oldXml.replaceAll("(?s)<cooler_names>.*?</cooler_names>\n", "");
        // As well as <stem_equivs>
        oldXml = oldXml.replaceAll("(?s)<stem_equivs>.*?</stem_equivs>\n", "");
        // As well as <stop_words>
        oldXml = oldXml.replaceAll("(?s)<stop_words>.*?</stop_words>\n", "");
        // As well as <qsup> for query blending
        oldXml = oldXml.replaceAll("(?s)<qsup.*?>.*?</qsup>\n", "");
        // As well as <km_from_origin> for geo-related features
        oldXml = oldXml.replaceAll("(?s)<km_from_origin>.*?</km_from_origin>\n", "");
        // As well as <entitylist />
        oldXml = oldXml.replaceAll("(?s)<entitylist>.*?</entitylist>\n", "");
        // As well as <svgs />
        oldXml = oldXml.replaceAll("(?s)<svgs>.*?</svgs>\n", "");
        // As well as <datecount>
        oldXml = oldXml.replaceAll("(?s)<datecount.*?>.*?</datecount>\n", "");
        // As well as <estimated_counts>
        oldXml = oldXml.replaceAll("(?s)<estimated_counts>.*?</estimated_counts>\n", "");
        // As well as <query_raw>
        oldXml = oldXml.replaceAll("(?s)<query_raw>.*?</query_raw>\n", "");
        // As well as <query_system_raw>
        oldXml = oldXml.replaceAll("(?s)<query_system_raw>.*?</query_system_raw>\n", "");
        // Remove <unexpected_tag> used for other tests
        oldXml = oldXml.replaceAll("(?s)<unexpected_tag>.*?</unexpected_tag>\n", "");
        
        // Year only dates (e.g. "1991") get parsed as 1st of Jan
        // and serialized back as dd MMM yyyy
        oldXml = oldXml.replaceAll("<date>1991</date>", "<date>1 Jan 1991</date>");
        
        // Saxon does not preserve the extra new line between <results> and <result>
        oldXml = oldXml.replaceAll("<results>\r\n\r\n<result>", "<results>\r\n<result>");
        oldXml = oldXml.replaceAll("<results>\n\n<result>", "<results>\n<result>");
        
        // Saxon seems to add a new line just before  along attribute.
        actual = actual.replaceAll("<cluster_nav level=\"0\"\r?\nurl=", "<cluster_nav level=\"0\" url=");

        // Saxon seems to add a new line after a long attribute
        p = Pattern.compile("<cluster href=\"(.*)\"\n");
        actual = p.matcher(actual).replaceAll("<cluster href=\"$1\" ");
        
        // Ensure we're not comparing empty or very small strings
        Assert.assertTrue(oldXml.length() > 50000);
        Assert.assertTrue(actual.length() > 50000);
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
            String rmcs[] = m.group(0).split("\n");
            Arrays.sort(rmcs);
            return m.replaceAll(StringUtils.join(rmcs, "\n"));
        }
        return input;
    }
}
