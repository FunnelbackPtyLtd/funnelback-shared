package com.funnelback.publicui.search.model.curator;

import com.funnelback.publicui.search.model.curator.action.AddQueryTerm;
import com.funnelback.publicui.search.model.curator.action.DisplayMessage;
import com.funnelback.publicui.search.model.curator.action.DisplayProperties;
import com.funnelback.publicui.search.model.curator.action.DisplayUrlAdvert;
import com.funnelback.publicui.search.model.curator.action.GroovyAction;
import com.funnelback.publicui.search.model.curator.action.PromoteUrls;
import com.funnelback.publicui.search.model.curator.action.RemoveUrls;
import com.funnelback.publicui.search.model.curator.action.ReplaceQueryTerm;
import com.funnelback.publicui.search.model.curator.action.SetQuery;
import com.funnelback.publicui.search.model.curator.action.TransformQuery;
import com.funnelback.publicui.search.model.curator.trigger.AllQueryWordsTrigger;
import com.funnelback.publicui.search.model.curator.trigger.AndTrigger;
import com.funnelback.publicui.search.model.curator.trigger.CountryNameTrigger;
import com.funnelback.publicui.search.model.curator.trigger.DateRangeTrigger;
import com.funnelback.publicui.search.model.curator.trigger.ExactQueryTrigger;
import com.funnelback.publicui.search.model.curator.trigger.FacetSelectionTrigger;
import com.funnelback.publicui.search.model.curator.trigger.GroovyTrigger;
import com.funnelback.publicui.search.model.curator.trigger.NotAnyTrigger;
import com.funnelback.publicui.search.model.curator.trigger.OrTrigger;
import com.funnelback.publicui.search.model.curator.trigger.QueryRegularExpressionTrigger;
import com.funnelback.publicui.search.model.curator.trigger.QuerySubstringTrigger;
import com.funnelback.publicui.search.model.curator.trigger.RequestParameterTrigger;
import com.funnelback.publicui.search.model.curator.trigger.UserSegmentTrigger;

public class CuratorClasses {

    public static Class<?>[] getTriggerClasses() {
        return new Class[] { AllQueryWordsTrigger.class, AndTrigger.class,
            RequestParameterTrigger.class, CountryNameTrigger.class, DateRangeTrigger.class,
            ExactQueryTrigger.class, FacetSelectionTrigger.class, GroovyTrigger.class,
            NotAnyTrigger.class, OrTrigger.class, QueryRegularExpressionTrigger.class,
            QuerySubstringTrigger.class, UserSegmentTrigger.class };
    }

    public static Class<?>[] getActionClasses() {
        return new Class[] { AddQueryTerm.class, DisplayMessage.class, DisplayProperties.class,
            DisplayUrlAdvert.class, GroovyAction.class, PromoteUrls.class, RemoveUrls.class,
            ReplaceQueryTerm.class, SetQuery.class, TransformQuery.class };
    }

}
