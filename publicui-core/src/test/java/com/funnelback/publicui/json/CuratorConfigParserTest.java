package com.funnelback.publicui.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import org.codehaus.jackson.JsonParseException;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.action.DisplayMessage;
import com.funnelback.publicui.search.model.curator.action.DisplayProperties;
import com.funnelback.publicui.search.model.curator.action.DisplayUrlAdvert;
import com.funnelback.publicui.search.model.curator.action.GroovyAction;
import com.funnelback.publicui.search.model.curator.action.PromoteUrls;
import com.funnelback.publicui.search.model.curator.action.RemoveUrls;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.curator.config.TriggerActions;
import com.funnelback.publicui.search.model.curator.data.Message;
import com.funnelback.publicui.search.model.curator.data.Properties;
import com.funnelback.publicui.search.model.curator.data.UrlAdvert;
import com.funnelback.publicui.search.model.curator.trigger.AllQueryWordsTrigger;
import com.funnelback.publicui.search.model.curator.trigger.AndTrigger;
import com.funnelback.publicui.search.model.curator.trigger.CountryNameTrigger;
import com.funnelback.publicui.search.model.curator.trigger.DateRangeTrigger;
import com.funnelback.publicui.search.model.curator.trigger.ExactQueryTrigger;
import com.funnelback.publicui.search.model.curator.trigger.GroovyTrigger;
import com.funnelback.publicui.search.model.curator.trigger.NotAnyTrigger;
import com.funnelback.publicui.search.model.curator.trigger.OrTrigger;
import com.funnelback.publicui.search.model.curator.trigger.QueryRegularExpressionTrigger;
import com.funnelback.publicui.search.model.curator.trigger.QuerySubstringTrigger;

public class CuratorConfigParserTest {

    /**
     * Tests that JSON serialisation and deserialisation of a curator config 'with the works' works.
     * @throws IOException 
     * @throws JsonParseException 
     * @throws ParseException 
     */
    @Test
    public void testRoundtrip() throws JsonParseException, IOException, ParseException {
        // Used several times
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("k1", "v1");
        properties.put("k2", "v2");
        
        DateFormat df = new SimpleDateFormat("yyyy-mm-dd");

        CuratorConfig config = new CuratorConfig();
        
        List<TriggerActions> triggerActions = new ArrayList<TriggerActions>();

        TriggerActions kitchenSink = new TriggerActions();
        kitchenSink.setId("ID");
        kitchenSink.setName("Name");
        kitchenSink.setEnabled(false);
        
        List<Trigger> triggers = new ArrayList<Trigger>();
        triggers.add(wrapInOr(new AllQueryWordsTrigger(Arrays.asList(new String[]{"a"}))));
        triggers.add(wrapInOr(new CountryNameTrigger(new HashSet<String>(Arrays.asList(new String[]{"Australia"})))));
        triggers.add(wrapInOr(new DateRangeTrigger(df.parse("2013-01-01"), df.parse("2014-01-01"))));
        triggers.add(wrapInOr(new ExactQueryTrigger("exactQuery", true)));
        triggers.add(wrapInOr(new QueryRegularExpressionTrigger("foo?")));
        triggers.add(wrapInOr(new QuerySubstringTrigger("substring")));

        List<Trigger> subTriggers = new ArrayList<Trigger>();
        subTriggers.add(new DateRangeTrigger(df.parse("2013-01-01"), df.parse("2014-01-01")));
        
        triggers.add(wrapInOr(new NotAnyTrigger(subTriggers)));

        triggers.add(new GroovyTrigger("src/test/resources/curator/TestTrigger.groovy", properties));

        kitchenSink.setTrigger(new AndTrigger(triggers));

        List<Action> actions = new ArrayList<Action>();
        actions.add(new DisplayMessage(new Message("html", properties, "category")));
        actions.add(new DisplayProperties(new Properties(properties, "category")));
        actions.add(new DisplayUrlAdvert(new UrlAdvert("title", "displayUrl", "linkUrl", "description", properties, "category"), true));
        actions.add(new GroovyAction("src/test/resources/curator/TestAction.groovy", properties));
        actions.add(new PromoteUrls(Arrays.asList(new String[]{"url"})));
        actions.add(new RemoveUrls(Arrays.asList(new String[]{"url"})));
        
        kitchenSink.setActions(actions);

        triggerActions.add(kitchenSink);
        
        config.setTriggerActions(triggerActions);
        
        
        CuratorConfigParser parser = new CuratorConfigParser();
        String json = parser.generateJsonCuratorConfiguration(config);
        
        CuratorConfig roundtripConfig = parser.parseJsonCuratorConfiguration(new ByteArrayInputStream(json.getBytes()));

        String json2 = parser.generateJsonCuratorConfiguration(roundtripConfig);
        
        Assert.assertEquals("Expected before and after JSON to match", json, json2);

        //System.out.println(json2);
    }

    private Trigger wrapInOr(Trigger trigger) {
        List<Trigger> triggers = new ArrayList<Trigger>();
        triggers.add(trigger);
        OrTrigger result = new OrTrigger(triggers);

        return result;
    }
}
