package com.funnelback.publicui.utils;

import static com.funnelback.common.function.Predicates.containedBy;
import static com.funnelback.common.function.Predicates.not;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.BB;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.CNTO;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.CONTEXTUAL_NAVIGATION;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.COOL;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.DAAT_TIMEOUT;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.EXPLAIN;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.KMOD;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.NEARDUP;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.QL;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SAME_COLLECTION_SUPPRESSION;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SAME_META_SUPPRESSION;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SBL;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SCO;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SF;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SM;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SORT;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SSS;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.TITLE_DUP_FACTOR;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;




public class PadreOptionsForSpeedTest {

    private PadreOptionsForSpeed padreOptsForSpeed = new PadreOptionsForSpeed();
    
    @Test
    public void testOptionsForCounting() {
        Set<String> expectedExtraOptions = ImmutableSet.of(
            COOL,
            KMOD,
            DAAT_TIMEOUT,
            SCO,
            CNTO,
            CONTEXTUAL_NAVIGATION,
            SBL,
            SF,
            SM,
            EXPLAIN,
            SORT,
            SSS,
            NEARDUP,
            TITLE_DUP_FACTOR,
            SAME_COLLECTION_SUPPRESSION,
            SAME_META_SUPPRESSION,
            BB,
            QL
            );
        
        List<String> leftOverOptions = padreOptsForSpeed.getOptionsThatDoNotAffectResultSetAsPairs()
            .stream()
            .map(o -> o.getOption())
            .filter(not(containedBy(expectedExtraOptions)))
            .filter(not(containedBy(padreOptsForSpeed.getOptionsForCounting())))
            .collect(Collectors.toList());
        
        Assert.assertEquals("A new option has been added to the list of options to speed up padre.\n"
            + "If the option is involved counting or might be used for faceted navigation then add it to\n"
            + " getOptionsForCounting() otherwise add it to the list of expectedExtraOptions\n"
            + "Options are: " + StringUtils.join(leftOverOptions, " "),
            0, leftOverOptions.size());
    }
}
