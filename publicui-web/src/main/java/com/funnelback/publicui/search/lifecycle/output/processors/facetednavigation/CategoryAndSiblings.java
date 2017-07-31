package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import java.util.List;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class CategoryAndSiblings {
    
    /**
     * A Category
     */
    @Getter private final Facet.Category category;
    
    /**
     * The siblings of the category (does not include the category).
     */
    @Getter private final List<Category> siblings;
    
    /**
     * 
     * @param siblingCategories A list of sibling categories, e.g. the list of Gscope categories
     * at the top level of the facet.
     * @return A list of each category with its siblings.
     */
    public static List<CategoryAndSiblings> toCategoriesWithSiblings(List<Facet.Category> siblingCategories) {
        return siblingCategories.stream().map(c -> new CategoryAndSiblings(c, 
            siblingCategories.stream().filter(sibling -> sibling != c).collect(Collectors.toList()))).collect(Collectors.toList());
    }
}