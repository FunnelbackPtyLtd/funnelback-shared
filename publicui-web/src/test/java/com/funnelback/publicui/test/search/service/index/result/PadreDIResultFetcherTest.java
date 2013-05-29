package com.funnelback.publicui.test.search.service.index.result;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.service.index.result.PadreDIResultFetcher;
import junit.framework.Assert;
import org.apache.commons.exec.OS;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Map;

public class PadreDIResultFetcherTest {

    private final static String EXT = OS.isFamilyWindows() ? ".bat" : ".sh";
    private PadreDIResultFetcher fetcher;

    @Before
    public void before() {
        fetcher = new PadreDIResultFetcher();
        fetcher.setSearchHome(new File("src/test/resources/dummy-search_home"));
        fetcher.setPadreDiExecutable(new File("src/test/resources/dummy-search_home/bin/mock-padre-di"+EXT));

        PropertyConfigurator.configure(DefaultValues.DEFAULT_LOG4J_CONSOLE_DEBUG_PROPERTIES);
    }

    @Test
    public void testUrlNotFound() {
        fetcher.setPadreDiExecutable(new File("src/test/resources/dummy-search_home/bin/mock-padre-di-nores"+EXT));
        Assert.assertNull(fetcher.fetchResult(new File("dummy"), URI.create("http://server.com/non-existent")));
    }

    @Test
    public void test() {
        Result r = fetcher.fetchResult(new File("dummy"), URI.create("http://courses.mq.edu.au/undergraduate/honours/honours-degree-of-bachelor-of-psychology"));

        Assert.assertNotNull(r);
        Assert.assertEquals("Honours degree of Bachelor of Psychology - Domestic - Macquarie University", r.getTitle());
        Assert.assertEquals("Psychology is the science of the human mind.  It looks at why people behave the way they do and what motivates behaviours and social trends. This specialist degree includes a fourth year of study where you'll complete a research thesis under the supervision of academic staff.  The aim is to align psychology honours students with world-class research supervisors who share the same interests.You'll study advanced topics in psychology in this highly structured program.  These include developmental psychol",
                r.getSummary());

        Map<String, String> md = r.getMetaData();
        Assert.assertEquals(6, md.size());

        Assert.assertEquals("Macquarie University, Sydney Australia", md.get("a"));
        Assert.assertEquals("94.05", md.get("A"));
        Assert.assertEquals("4 years full-time", md.get("B"));
        Assert.assertEquals("300126", md.get("D"));
        Assert.assertEquals("If you are analytical and inquisitive, and are interested in pursuing further graduate study in psychology. ", md.get("E"));
        Assert.assertEquals("6300", md.get("F"));
    }
    
}
