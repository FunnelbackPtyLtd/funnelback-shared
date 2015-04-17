package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.List;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>Applies white and black list to faceted navigation categories.</p>
 */
@Component("facetedNavigationWhiteBlackListOutputProcessor")
@Log4j2
public class FacetedNavigationWhiteBlackList extends AbstractOutputProcessor {

    private static final String SEP = ",";
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && SearchTransactionUtils.hasResponse(searchTransaction)
                && searchTransaction.getResponse().getFacets().size() > 0) {
            
            Config config = searchTransaction.getQuestion().getCollection().getConfiguration();
            for (Facet f: searchTransaction.getResponse().getFacets()) {
                String[] whiteList = getCombinedList(
                        config.value(Keys.FacetedNavigation.WHITE_LIST+"."+f.getName()),
                        config.value(Keys.FacetedNavigation.WHITE_LIST));

                if (whiteList.length > 0) {
                    applyWhiteList(f.getCategories(), whiteList);
                    log.debug("Applied white list '"+StringUtils.join(whiteList, SEP)+ "'"
                            + " to facet '" + f.getName() + "'");
                }
            
                String[] blackList = getCombinedList(
                        config.value(Keys.FacetedNavigation.BLACK_LIST+"."+f.getName()),
                        config.value(Keys.FacetedNavigation.BLACK_LIST));

                if (blackList.length > 0) {
                    applyBlackList(f.getCategories(), blackList);
                    log.debug("Applied black list '"+StringUtils.join(blackList, SEP)+"'"
                            + " to facet '" + f.getName() + "'");
                }                
            }
        }
    }

    private String[] getCombinedList(final String list1, final String list2) {
        if (list1 == null && list2 == null) {
            return new String[0];
        } else if (list1 == null) {
            return list2.toLowerCase().split(SEP);
        } else if (list2 == null) {
            return list1.toLowerCase().split(SEP);
        } else {
            return (String[]) ArrayUtils.addAll(
                    list1.toLowerCase().split(","),
                    list2.toLowerCase().split(","));
        }
    }
     
    /**
     * <p>Applies a white list recursively to facets categories values.</p>
     * <p>Remove any value not contained in the white list.</p>
     * @param categories
     * @param whiteList
     */
    private void applyWhiteList(final List<Category> categories, final String[] whiteList) {
        for (Category c: categories) {
            CollectionUtils.filter(c.getValues(), new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    CategoryValue cv = (CategoryValue) object;
                    return ArrayUtils.contains(whiteList, cv.getLabel().toLowerCase());
                }
            });
            applyWhiteList(c.getCategories(), whiteList);
        }
    }
    
    /**
     * <p>Applies a black list recursively to facets categories values.</p>
     * <p>Remove any value contained in the black list.</p>
     * @param categories
     * @param blackList
     */
    private void applyBlackList(final List<Category> categories, final String[] blackList) { 
        for (Category c: categories) {
            CollectionUtils.filter(c.getValues(), new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    CategoryValue cv = (CategoryValue) object;
                    return ! ArrayUtils.contains(blackList, cv.getLabel().toLowerCase());
                }
            });
            applyBlackList(c.getCategories(), blackList);
        }    
    }
}
