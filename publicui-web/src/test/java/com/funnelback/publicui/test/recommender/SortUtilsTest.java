package com.funnelback.publicui.test.recommender;

import com.funnelback.publicui.recommender.utils.SortUtils;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SortUtilsTest {

    private ItemTuple addRecommendation(String itemID, ItemTuple.Source source, int rank, List<ItemTuple> recommendations) {
        ItemTuple itemTuple = new ItemTuple(itemID, source);
        itemTuple.setRank(rank);
        recommendations.add(itemTuple);

        return itemTuple;
    }

    @Test
   	public void testSort() throws Exception {
        List<ItemTuple> recommendations = new ArrayList<>();

        ItemTuple item0 =
                addRecommendation("http://medicalimaging.curtin.edu.au/", ItemTuple.Source.CO_CLICKS, 0, recommendations);
        addRecommendation("http://medicalimaging.curtin.edu.au/", ItemTuple.Source.RELATED_CLICKS, 0, recommendations);
        addRecommendation("http://medicalimaging.curtin.edu.au/", ItemTuple.Source.RELATED_RESULTS, 0, recommendations);
        addRecommendation("http://medicalimaging.curtin.edu.au/", ItemTuple.Source.EXPLORE_RESULTS, 0, recommendations);

        ItemTuple item1 = addRecommendation("http://healthsciences.curtin.edu.au/teaching/medical_education.cfm",
                ItemTuple.Source.RELATED_CLICKS, 3, recommendations);
        ItemTuple item2 = addRecommendation("http://unilife.curtin.edu.au/MedImaging.htm",
                ItemTuple.Source.RELATED_CLICKS, 2, recommendations);
        ItemTuple item3 = addRecommendation("http://iap.curtin.edu.au/", ItemTuple.Source.RELATED_CLICKS, 2, recommendations);
        ItemTuple item4 = addRecommendation("http://handbook.curtin.edu.au/", ItemTuple.Source.RELATED_CLICKS, 2, recommendations);
        ItemTuple item5 = addRecommendation("http://courses.curtin.edu.au/course_overview/undergraduate/oral-health-therapy",
                ItemTuple.Source.RELATED_CLICKS, 1, recommendations);
        ItemTuple item6
                = addRecommendation("http://handbook.curtin.edu.au/academic.html", ItemTuple.Source.RELATED_CLICKS, 1, recommendations);
        ItemTuple item7 = addRecommendation("http://courses.curtin.edu.au/course_overview/undergraduate/occupational-therapy",
                ItemTuple.Source.CO_CLICKS, 4, recommendations);
        ItemTuple item8 = addRecommendation("http://handbook.curtin.edu.au/units/31/311338.html",
                ItemTuple.Source.CO_CLICKS, 1, recommendations);
        ItemTuple item9 = addRecommendation("http://courses.curtin.edu.au/course_overview/all-courses-search.cfm",
                ItemTuple.Source.CO_CLICKS, 3, recommendations);
        ItemTuple item10 = addRecommendation("http://courses.curtin.edu.au/course_overview/undergraduate/physiotherapy",
                ItemTuple.Source.CO_CLICKS, 2, recommendations);
        ItemTuple item11 = addRecommendation("http://handbook.curtin.edu.au/units/31/311338.html",
                ItemTuple.Source.EXPLORE_RESULTS, 0, recommendations);
        ItemTuple item12 = addRecommendation("http://handbook.curtin.edu.au/units/example1.html",
                ItemTuple.Source.EXPLORE_RESULTS, 3, recommendations);
        ItemTuple item13 = addRecommendation("http://handbook.curtin.edu.au/units/example1.html",
                ItemTuple.Source.EXPLORE_RESULTS, 0, recommendations);
        ItemTuple item14 = addRecommendation("http://handbook.curtin.edu.au/units/32/678436.html",
                ItemTuple.Source.EXPLORE_RESULTS, 4, recommendations);
        ItemTuple item15 = addRecommendation("http://courses.curtin.edu.au/course_overview/undergraduate/forensic-medicine",
                ItemTuple.Source.RELATED_RESULTS, 2, recommendations);
        ItemTuple item16 = addRecommendation("http://courses.curtin.edu.au/course_overview/undergraduate/pathology",
                ItemTuple.Source.RELATED_RESULTS, 1, recommendations);

        ItemTuple[] expectedOrder = {item0, item8, item10, item9, item7, item5, item6, item2,
                item3, item4, item1, item16, item15, item12, item14};
        List<ItemTuple> sortedRecommendations = SortUtils.sortList(recommendations);

        for (ItemTuple itemTuple : sortedRecommendations) {
            System.out.println(itemTuple);
        }

        Assert.assertEquals(15, sortedRecommendations.size());
        checkSortOrder(sortedRecommendations, expectedOrder);
    }

    private void checkSortOrder(List<ItemTuple> sortedRecommendations, ItemTuple[] expectedOrder) {
        int i = 0;

        for (ItemTuple itemTuple : sortedRecommendations) {
            Assert.assertEquals(expectedOrder[i], itemTuple);
            i++;
        }
    }
}
