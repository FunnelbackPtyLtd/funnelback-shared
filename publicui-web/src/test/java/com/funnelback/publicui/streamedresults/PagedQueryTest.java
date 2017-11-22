package com.funnelback.publicui.streamedresults;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingExceptionPacketSizeTooBig;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.streamedresults.PagedQuery.StartRankAndNumRank;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;
import com.funnelback.publicui.utils.PadreOptionsForSpeed.OptionAndValue;
import com.google.common.collect.ImmutableMap;

import lombok.Setter;

public class PagedQueryTest {

    
    public SearchQuestion getSearchQuestion(int numRanks, int startRank) {
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put("num_ranks", new String[]{numRanks + ""});
        question.getRawInputParameters().put("start_rank", new String[]{startRank + ""});
        return question;
    }
    @Test
    public void applyOptimisationsTestOptimisationsEnabled() {
        PadreOptionsForSpeed padreOptionsForSpeed = mock(PadreOptionsForSpeed.class);
        when(padreOptionsForSpeed.getOptionsThatDoNotAffectResultSetAsPairs())
            .thenReturn(asList(new OptionAndValue("foo", "bar"), new OptionAndValue("existing", "bar")));
        
        when(padreOptionsForSpeed.getHighServiceVolumeOptionAsPair())
            .thenReturn(new OptionAndValue("other", "other"));
        
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put("existing", new String[]{"something"});
        
        PagedQuery pagedQuery = new PagedQuery(question, false);
        
        pagedQuery.applyOptimisations(question, padreOptionsForSpeed);
        
        Assert.assertEquals("Optimisations should not replace user set values",
            "something", question.getRawInputParameters().get("existing")[0]);
        
        Assert.assertEquals("bar", question.getRawInputParameters().get("foo")[0]);
        Assert.assertEquals("other", question.getRawInputParameters().get("other")[0]);
    }
    
    @Test
    public void applyOptimisationsTestOptimisationsDisabled() {
        PadreOptionsForSpeed padreOptionsForSpeed = mock(PadreOptionsForSpeed.class);
        
        
        SearchQuestion question = new SearchQuestion();
        
        
        PagedQuery pagedQuery = new PagedQuery(question, true);
        
        pagedQuery.applyOptimisations(question, padreOptionsForSpeed);
        
        verify(padreOptionsForSpeed, never()).getHighServiceVolumeOptionAsPair();
        verify(padreOptionsForSpeed, never()).getOptionsThatDoNotAffectResultSetAsPairs();
        
        Assert.assertEquals(0, question.getRawInputParameters().size());
    }
    
    @Test
    public void numRanksForNextRequestTest() {
        Optional<Integer> res;
        PagedQuery oneDocPagedQuery = new PagedQuery(getSearchQuestion(1, 1), false);
        res = oneDocPagedQuery.numRanksForNextRequestThatDoesNotExceedDocsWanted(1, 1);
        Assert.assertTrue(res.isPresent());
        Assert.assertEquals(1, res.get() + 0);
        res = oneDocPagedQuery.numRanksForNextRequestThatDoesNotExceedDocsWanted(1, 2);
        Assert.assertTrue(res.isPresent());
        Assert.assertEquals(1, res.get() + 0);
        res = oneDocPagedQuery.numRanksForNextRequestThatDoesNotExceedDocsWanted(2, 1);
        Assert.assertFalse(res.isPresent());
        
        PagedQuery pagedQuery = new PagedQuery(getSearchQuestion(10, 1), false);
        res = pagedQuery.numRanksForNextRequestThatDoesNotExceedDocsWanted(1, 1);
        Assert.assertTrue(res.isPresent());
        Assert.assertEquals(1, res.get() + 0);
        
        res = pagedQuery.numRanksForNextRequestThatDoesNotExceedDocsWanted(10, 10);
        Assert.assertTrue(res.isPresent());
        Assert.assertEquals(1, res.get() + 0);
        
        res = pagedQuery.numRanksForNextRequestThatDoesNotExceedDocsWanted(11, 10);
        Assert.assertFalse(res.isPresent());
    }
    
    @Test
    public void calculateTargetNumRanksTestNulls() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        Assert.assertEquals(-12, pagedQuery.calculateTargetNumRanks(null, -12));
        
        SearchTransaction transaction = mock(SearchTransaction.class);
        Assert.assertEquals(-12, pagedQuery.calculateTargetNumRanks(transaction, -12));
        
        when(transaction.getResponse()).thenReturn(mock(SearchResponse.class));
        when(transaction.getResponse().getUntruncatedPadreOutputSize())
            .thenReturn(0)
            .thenReturn(null);
        
        Assert.assertEquals(-12, pagedQuery.calculateTargetNumRanks(transaction, -12));
        Assert.assertEquals(-12, pagedQuery.calculateTargetNumRanks(transaction, -12));
    }
    
    @Test
    public void calculateTargetNumRanksTestZero() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        pagedQuery.setTargetResponseSize(1000);
        SearchTransaction transaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
       
        transaction.getResponse().setUntruncatedPadreOutputSize(100);
        
        // Note as we don't have any results the calculation will revert back to 
        // the given num ranks to work out what it should be.
        int numRanks = pagedQuery.calculateTargetNumRanks(transaction, 0);
        
        Assert.assertEquals(10, numRanks);
    }
    
    @Test
    public void calculateTargetNumRanksTestOneNumRank() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        pagedQuery.setTargetResponseSize(10000);
        SearchTransaction transaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
       
        transaction.getResponse().setUntruncatedPadreOutputSize(100);
        
        // Note as we don't have any results the calculation will revert back to 
        // the given num ranks to work out what it should be.
        int numRanks = pagedQuery.calculateTargetNumRanks(transaction, 1);
        
        Assert.assertEquals(100, numRanks);
    }
    
    @Test
    public void calculateTargetNumRanksTestUsesNumnberOfResults() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        pagedQuery.setTargetResponseSize(18);
        SearchTransaction transaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
       
        transaction.getResponse().setUntruncatedPadreOutputSize(9);
        transaction.getResponse().setResultPacket(new ResultPacket());
        transaction.getResponse().getResultPacket().getResults().add(mock(Result.class));
        transaction.getResponse().getResultPacket().getResults().add(mock(Result.class));
        transaction.getResponse().getResultPacket().getResults().add(mock(Result.class));
        
        // Note as we don't have any results the calculation will revert back to 
        // the given num ranks to work out what it should be.
        int numRanks = pagedQuery.calculateTargetNumRanks(transaction, 1);
        
        Assert.assertEquals(6, numRanks);
    }
    
    @Test
    public void padrePacketWasTooLargeTestWasTooBig() {
        Exception e = new Exception("", new Exception("", new PadreForkingExceptionPacketSizeTooBig("", 21)));
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        Assert.assertTrue(pagedQuery.padrePacketWasTooLarge(e));
        Assert.assertTrue(pagedQuery.padrePacketWasTooLarge(e.getCause()));
        Assert.assertTrue(pagedQuery.padrePacketWasTooLarge(e.getCause().getCause()));
    }
    
    @Test
    public void padrePacketWasTooLargeTestSomeOtherError() {
        Exception e = new Exception("", new Exception("", new Exception("")));
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        Assert.assertFalse(pagedQuery.padrePacketWasTooLarge(e));
        Assert.assertFalse(pagedQuery.padrePacketWasTooLarge(e.getCause()));
        Assert.assertFalse(pagedQuery.padrePacketWasTooLarge(e.getCause().getCause()));
        
        Assert.assertFalse(pagedQuery.padrePacketWasTooLarge(null));
    }
    
    @Test
    public void getLastIntFromMap() {
        // Test cases where the map does not have the data we want in the form we are expecting
        Assert.assertFalse(PagedQuery.getLastIntFromMap(ImmutableMap.of(), "foo").isPresent());
        Assert.assertFalse(PagedQuery.getLastIntFromMap(ImmutableMap.of("foo", new String[0]), "foo").isPresent());
        Assert.assertFalse(PagedQuery.getLastIntFromMap(ImmutableMap.of("foo", new String[]{"bar"}), "foo").isPresent());
        Assert.assertFalse(PagedQuery.getLastIntFromMap(ImmutableMap.of("foo", new String[]{"12.2"}), "foo").isPresent());
        
        Optional<Integer> value = PagedQuery.getLastIntFromMap(ImmutableMap.of("foo", new String[]{"12"}), "foo");
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals(12, value.get() + 0);
    }
    
    @Test
    public void getMaxResponseSizeBasedOnJVMVerySmallMemory() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        Assert.assertEquals(5*1024*1024, pagedQuery.getMaxResponseSizeBasedOnJVM(10));
    }
    
    @Test
    public void getMaxResponseSizeBasedOnJVMVeryMassiveMemory() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        
        Assert.assertEquals("If we have a gigantic amount of memory we need to limit the memory to"
            + "max int as java arrays can not handle more than that.",
            Integer.MAX_VALUE, pagedQuery.getMaxResponseSizeBasedOnJVM(Long.MAX_VALUE));
    }
    
    @Test
    public void getMaxResponseSizeBasedOnJVM512MB() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        
        int maxResponseSize = pagedQuery.getMaxResponseSizeBasedOnJVM(512*1024*1024);
        
        // About 50MB is the max size.
        Assert.assertEquals(50*1024*1024, maxResponseSize, 5 * 1024 * 1024);
        
        // About 40MB
        Assert.assertEquals(40*1024*1024, pagedQuery.calculateTargetRersponseSize(maxResponseSize), 2 * 1024 * 1024);
    }
    
    @Test
    public void testIgnoreNegativeNumRanks() {
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put("num_ranks", new String[]{"-1"});
        
        Assert.assertEquals(Integer.MAX_VALUE, new PagedQuery(question, false).getNumDocsWanted());
    }
    
    @Test
    public void testIgnoreNegativeStartRank() {
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put("start_rank", new String[]{"-1"});
        
        Assert.assertEquals(1, new PagedQuery(question, false).getInitialStartRank());
    }
    
    @Test
    public void testDaatDepthCalculations() {
        // Test daat depths for differnt values of num_ranks and start_rank
        Assert.assertEquals(1, new PagedQuery(getSearchQuestion(1, 1), false).getDaatDepth());
        Assert.assertEquals(12, new PagedQuery(getSearchQuestion(1, 12), false).getDaatDepth());
        Assert.assertEquals(10, new PagedQuery(getSearchQuestion(10, 1), false).getDaatDepth());
        Assert.assertEquals(20, new PagedQuery(getSearchQuestion(10, 11), false).getDaatDepth());
        
        Assert.assertEquals(Integer.MAX_VALUE, 
            new PagedQuery(getSearchQuestion(10, Integer.MAX_VALUE), false).getDaatDepth());
        Assert.assertEquals(Integer.MAX_VALUE, 
            new PagedQuery(getSearchQuestion(Integer.MAX_VALUE, 10), false).getDaatDepth());
        
        Assert.assertEquals(Integer.MAX_VALUE, 
            new PagedQuery(getSearchQuestion(Integer.MAX_VALUE, Integer.MAX_VALUE), false).getDaatDepth());
    }
    
    @Test
    public void createQuestionForNextRequestTest() {
        SearchQuestion qestion = new SearchQuestion();
        qestion.setQuery("the q");
        qestion.setQuestionType(SearchQuestionType.ACCESSIBILITY_AUDITOR_GET_ALL_RESULTS);
        
        PagedQuery pagedQuery = spy(new PagedQuery(qestion, false));
        when(pagedQuery.getDaatDepth()).thenReturn(1337);
        pagedQuery.setMaxResponseSize(121);
        
        // We expect these to be over-ridden
        qestion.getRawInputParameters().put("num_ranks", new String[]{"666"});
        qestion.getRawInputParameters().put("start_rank", new String[]{"666"});
        qestion.getRawInputParameters().put("daat", new String[]{"666"});
        
        SearchQuestion newQuestion = pagedQuery.createQuestionForNextRequest(20, 1000);
        
        Assert.assertNotSame("Should have created a new question", newQuestion, qestion);
        Assert.assertEquals("the q", newQuestion.getQuery());
        Assert.assertEquals(SearchQuestionType.ACCESSIBILITY_AUDITOR_GET_ALL_RESULTS, newQuestion.getQuestionType());
        
        Assert.assertTrue(newQuestion.getMaxPadrePacketSize().isPresent());
        Assert.assertEquals(121, newQuestion.getMaxPadrePacketSize().get() + 0);
        
        Assert.assertEquals("1000", newQuestion.getRawInputParameters().get("num_ranks")[0]);
        Assert.assertEquals("20", newQuestion.getRawInputParameters().get("start_rank")[0]);
        Assert.assertEquals("1337", newQuestion.getRawInputParameters().get("daat")[0]);
        
        Assert.assertTrue(newQuestion.getDynamicQueryProcessorOptions().contains("-daat_timeout=3600"));
        
        verify(pagedQuery).applyOptimisations(eq(newQuestion), any());
    }
    
    @Test
    public void shouldFetchMoreResultsNullTest() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        Assert.assertFalse(pagedQuery.shouldFetchMoreResults(1, -100, null));
        
        SearchTransaction transaction = mock(SearchTransaction.class);
        Assert.assertFalse(pagedQuery.shouldFetchMoreResults(1, -100, transaction));
        
        when(transaction.getResponse()).thenReturn(mock(SearchResponse.class));
        Assert.assertFalse(pagedQuery.shouldFetchMoreResults(1, -100, transaction));
        
        when(transaction.getResponse().getResultPacket()).thenReturn(mock(ResultPacket.class));
        Assert.assertFalse(pagedQuery.shouldFetchMoreResults(1, -100, transaction));
        
        when(transaction.getResponse().getResultPacket().getResults()).thenReturn(asList());
        Assert.assertFalse(pagedQuery.shouldFetchMoreResults(1, -100, transaction));
        
        when(transaction.getResponse().getResultPacket().getResultsSummary()).thenReturn(mock(ResultsSummary.class));
        when(transaction.getResponse().getResultPacket().getResultsSummary().getTotalMatching()).thenReturn(null);
        Assert.assertFalse(pagedQuery.shouldFetchMoreResults(1, -100, transaction));
    }
    
    @Test
    public void shouldFetchMoreResultsTest() {
        PagedQuery pagedQuery = new PagedQuery(new SearchQuestion(), false);
        
        
        SearchTransaction transaction = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
        when(transaction.getResponse().getResultPacket().getResultsSummary().getTotalMatching())
            .thenReturn(100);
        
        Assert.assertTrue(pagedQuery.shouldFetchMoreResults(1, 1, transaction));
        Assert.assertTrue("We just got the 99th result, get one more", pagedQuery.shouldFetchMoreResults(99, 1, transaction));
        Assert.assertFalse("We just got the 100th result", pagedQuery.shouldFetchMoreResults(100, 1, transaction));
        Assert.assertFalse("We just got the first 100 results.", pagedQuery.shouldFetchMoreResults(1, 100, transaction));
        Assert.assertFalse(pagedQuery.shouldFetchMoreResults(101, 1, transaction));
    }
    
    @Test
    public void calculateNumRanksOnErrorTest() {
        PagedQuery pagedQuery = spy(new PagedQuery(new SearchQuestion(), false));
        doReturn(100)
        .when(pagedQuery)
        .calculateTargetNumRanks(any(), anyInt());
        
        Assert.assertEquals("Should go with 1 as it is the smallest option.", 
            1, pagedQuery.calculateNumRanksOnError(2, new SearchTransaction()));
        
        Assert.assertEquals("Should go with 100 as it is the smallest option.", 
            100, pagedQuery.calculateNumRanksOnError(6000, new SearchTransaction()));
    }
    
    @Test
    public void hasRecoverableErrorTestNoError() {
        SearchTransaction transaction = mock(SearchTransaction.class);
        when(transaction.getError()).thenReturn(null);
        Assert.assertFalse(new PagedQuery(new SearchQuestion(), false).hasRecoverableError(221, transaction));
    }
    
    @Test
    public void hasRecoverableErrorTestWithErrorWithUnknownException() {
        SearchTransaction transaction = mock(SearchTransaction.class);
        when(transaction.getError()).thenReturn(mock(SearchError.class));
        when(transaction.getError().getAdditionalData()).thenReturn(new Exception());
        try {
            new PagedQuery(new SearchQuestion(), false).hasRecoverableError(221, transaction);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertEquals(transaction.getError().getAdditionalData(), e.getCause());
        }
    }
    
    @Test
    public void hasRecoverableErrorTestWithErrorNoException() {
        SearchTransaction transaction = mock(SearchTransaction.class);
        when(transaction.getError()).thenReturn(mock(SearchError.class));
        try {
            new PagedQuery(new SearchQuestion(), false).hasRecoverableError(222, transaction);
            Assert.fail();
        } catch (RuntimeException e) {
            
        }
    }
    
    @Test
    public void hasRecoverableErrorTestWithErrorWithTooLargePacketExceptionNumRank1() {
        SearchTransaction transaction = mock(SearchTransaction.class);
        when(transaction.getError()).thenReturn(mock(SearchError.class));
        when(transaction.getError().getAdditionalData()).thenReturn(new PadreForkingExceptionPacketSizeTooBig("",1));
        try {
            // If we did a search with num_ranks = 1 but the padre packet size was too big
            // then we must give up because we can not ask padre for half a document
            new PagedQuery(new SearchQuestion(), false).hasRecoverableError(1, transaction);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertEquals("A single document is bigger than the maximum PADRE result packet size "
                + "we are willing to accept, try increasing the amount of JVM memory.", e.getMessage());
        }
    }
    
    @Test
    public void hasRecoverableErrorTestWithErrorWithTooLargePacketException() {
        SearchTransaction transaction = mock(SearchTransaction.class);
        when(transaction.getError()).thenReturn(mock(SearchError.class));
        when(transaction.getError().getAdditionalData()).thenReturn(new PadreForkingExceptionPacketSizeTooBig("",1));
        Assert.assertTrue(new PagedQuery(new SearchQuestion(), false).hasRecoverableError(2, transaction));
    }
    
    
    public class TestablePagedQuery extends PagedQuery {
        
        @Setter public SearchQuestion questionCreated = mock(SearchQuestion.class);

        public TestablePagedQuery(SearchQuestion searchQuestion, boolean disableOptimisation) {
            super(searchQuestion, disableOptimisation);
        }
        
        public SearchQuestion createQuestionForNextRequest(int startRank, int numRank) {
            return questionCreated;
        }
        
    }
    
    private final Function<SearchQuestion, SearchTransaction> FAIL_IF_CALLED_SEARCH_EXECUTOR = (t) -> {
        Assert.fail();
        return null;
    };
    
    @Test
    public void runSearchTestWeHaveAllDocsWeNeed() {
        PagedQuery pagedQuery = spy(new PagedQuery(getSearchQuestion(100, 1), false));
        doReturn(Optional.empty()).when(pagedQuery).numRanksForNextRequestThatDoesNotExceedDocsWanted(100, 1);
        
        Optional<StartRankAndNumRank> res = 
            pagedQuery.runSearchAndGetStartRankAndNumRankForNextSearch(1000, 1, FAIL_IF_CALLED_SEARCH_EXECUTOR, null);
        
        Assert.assertFalse(res.isPresent());
    }
    
    @Test
    public void runSearchTestEverythingGoesWell() {
        PagedQuery pagedQuery = spy(new PagedQuery(getSearchQuestion(100, 1), false));
        doReturn(Optional.of(66)).when(pagedQuery).numRanksForNextRequestThatDoesNotExceedDocsWanted(1, 100);
        
        SearchQuestion searchQuestion = new SearchQuestion();
        doReturn(searchQuestion).when(pagedQuery).createQuestionForNextRequest(66, 1);
        
        SearchTransaction transaction = new SearchTransaction();
        
        doReturn(false).when(pagedQuery).hasRecoverableError(66, transaction);
        
        doReturn(true).when(pagedQuery).shouldFetchMoreResults(1, 66, transaction);
        
        doReturn(1000).when(pagedQuery).calculateTargetNumRanks(transaction, 66);
        
        Optional<StartRankAndNumRank> res =
        pagedQuery.runSearchAndGetStartRankAndNumRankForNextSearch(1, 100, (q) -> {
                return transaction;
            }, 
            (t) -> Assert.assertEquals(transaction, t.getValue()));
        
        Assert.assertTrue(res.isPresent());
        Assert.assertEquals("We fetched 66 results so we want to start at 67", 67, res.get().getStartRank());
        Assert.assertEquals(1000, res.get().getNumRank());
    }
    
    @Test
    public void runSearchTestNoMoreResultsToGet() {
        PagedQuery pagedQuery = spy(new PagedQuery(getSearchQuestion(100, 1), false));
        doReturn(Optional.of(66)).when(pagedQuery).numRanksForNextRequestThatDoesNotExceedDocsWanted(1, 100);
        
        SearchQuestion searchQuestion = new SearchQuestion();
        doReturn(searchQuestion).when(pagedQuery).createQuestionForNextRequest(66, 1);
        
        SearchTransaction transaction = new SearchTransaction();
        
        doReturn(false).when(pagedQuery).hasRecoverableError(66, transaction);
        
        // Say that we don't want to get more results.
        doReturn(false).when(pagedQuery).shouldFetchMoreResults(1, 66, transaction);
        
        doReturn(1000).when(pagedQuery).calculateTargetNumRanks(transaction, 66);
        
        Optional<StartRankAndNumRank> res =
        pagedQuery.runSearchAndGetStartRankAndNumRankForNextSearch(1, 100, (q) -> {
                return transaction;
            }, 
            (t) -> Assert.assertEquals(transaction, t.getValue()));
        
        Assert.assertFalse("This should be empty as we have all the results from the index.", res.isPresent());
    }
    
    @Test
    public void runSearchTestRecoverableErrorOccured() {
        PagedQuery pagedQuery = spy(new PagedQuery(getSearchQuestion(100, 1), false));
        doReturn(Optional.of(66)).when(pagedQuery).numRanksForNextRequestThatDoesNotExceedDocsWanted(1, 100);
        
        SearchQuestion searchQuestion = new SearchQuestion();
        doReturn(searchQuestion).when(pagedQuery).createQuestionForNextRequest(66, 1);
        
        SearchTransaction transaction = new SearchTransaction();
        
        doReturn(true).when(pagedQuery).hasRecoverableError(66, transaction);
        
        doReturn(22).when(pagedQuery).calculateNumRanksOnError(66, transaction);
        
        
        Optional<StartRankAndNumRank> res =
        pagedQuery.runSearchAndGetStartRankAndNumRankForNextSearch(1, 100, (q) -> {
                return transaction;
            }, 
            (t) -> Assert.fail("Should not have reached here as the transaction is in error"));
        
        Assert.assertTrue(res.isPresent());
        
        Assert.assertEquals("The request failed so we should start at the given start rank.", 1, res.get().getStartRank());
        Assert.assertEquals(22, res.get().getNumRank());
    }
    
    @Test
    public void runOnEachPageTest() {
        PagedQuery pagedQuery = spy(new PagedQuery(getSearchQuestion(33, 22), false));
        
        doReturn(Optional.empty()).when(pagedQuery)
            .runSearchAndGetStartRankAndNumRankForNextSearch(anyInt(), anyInt(), any(), any());
        
        pagedQuery.runOnEachPage(null, null);
        
        verify(pagedQuery).runSearchAndGetStartRankAndNumRankForNextSearch(eq(22), eq(100), any(), any());
    }
}