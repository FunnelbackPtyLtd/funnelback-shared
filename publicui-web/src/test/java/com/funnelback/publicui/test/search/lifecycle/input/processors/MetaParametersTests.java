package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MetaParameters;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MetaParametersTests {
    
    @Test
    public void testMissingData() throws InputProcessorException {
        MetaParameters processor = new MetaParameters();
        
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        processor.processInput(new SearchTransaction(question, null));        
    }
    
    @Test
    public void testNoMetaParameters() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
    }
    
    @Test
    public void testNoParameterValue() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put("meta_a", null);
        st.getQuestion().getRawInputParameters().put("smeta_a", null);
        st.getQuestion().getRawInputParameters().put("meta_c_or", new String[] {""});
        st.getQuestion().getRawInputParameters().put("smeta_c_or", new String[] {""});
        st.getQuestion().getRawInputParameters().put("meta_X_and", null);
        st.getQuestion().getRawInputParameters().put("smeta_X_and", null);
        st.getQuestion().getRawInputParameters().put("query_phrase", null);
        st.getQuestion().getRawInputParameters().put("squery_phrase", null);
        st.getQuestion().getRawInputParameters().put("query_and", new String[] {""});
        st.getQuestion().getRawInputParameters().put("squery_and", new String[] {""});
        st.getQuestion().getRawInputParameters().put("query_or", null);
        st.getQuestion().getRawInputParameters().put("squery_or", null);
        st.getQuestion().getRawInputParameters().put("unrelated", null);
        st.getQuestion().getRawInputParameters().put("sunrelated", null);
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
    }
    
    @Test
    public void testMetaMultipleWords() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

        st.getQuestion().getRawInputParameters().put("meta_a", new String[] {"simple operator"});
        st.getQuestion().getRawInputParameters().put("smeta_a", new String[] {"simple operator"});
        st.getQuestion().getRawInputParameters().put("meta_b_trunc", new String[] {"trunc operator"});
        st.getQuestion().getRawInputParameters().put("smeta_b_trunc", new String[] {"trunc operator"});
        st.getQuestion().getRawInputParameters().put("meta_c_orplus", new String[] {"orplus operator"});
        st.getQuestion().getRawInputParameters().put("smeta_c_orplus", new String[] {"orplus operator"});
        st.getQuestion().getRawInputParameters().put("meta_d_orsand", new String[] {"orsand operator"});
        st.getQuestion().getRawInputParameters().put("smeta_d_orsand", new String[] {"orsand operator"});
        st.getQuestion().getRawInputParameters().put("meta_e_or", new String[] {"or operator"});
        st.getQuestion().getRawInputParameters().put("smeta_e_or", new String[] {"or operator"});
        st.getQuestion().getRawInputParameters().put("meta_f_phrase", new String[] {"phrase \"operator\""});
        st.getQuestion().getRawInputParameters().put("smeta_f_phrase", new String[] {"phrase \"operator\""});
        st.getQuestion().getRawInputParameters().put("meta_g_prox", new String[] {"prox operator"});
        st.getQuestion().getRawInputParameters().put("smeta_g_prox", new String[] {"prox operator"});
        st.getQuestion().getRawInputParameters().put("meta_h_and", new String[] {"and operator"});
        st.getQuestion().getRawInputParameters().put("smeta_h_and", new String[] {"and operator"});
        st.getQuestion().getRawInputParameters().put("meta_I_sand", new String[] {"sand operator"});
        st.getQuestion().getRawInputParameters().put("smeta_I_sand", new String[] {"sand operator"});
        st.getQuestion().getRawInputParameters().put("meta_j_not", new String[] {"not operator"});
        st.getQuestion().getRawInputParameters().put("smeta_j_not", new String[] {"not operator"});
        st.getQuestion().getRawInputParameters().put("dummy", new String[] {"value"});
        st.getQuestion().getRawInputParameters().put("sdummy", new String[] {"value"});

        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(10, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("a:simple a:operator"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("b:*trunc* b:*operator*"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[c:orplus c:operator]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[d:orsand d:operator]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[e:or e:operator]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("f:\"phrase operator\""));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("g:`prox operator`"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+h:and +h:operator"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|I:sand |I:operator"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-j:not -j:operator"));
        
        Assert.assertEquals(10, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("a:simple a:operator"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("b:*trunc* b:*operator*"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+[c:orplus c:operator]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|[d:orsand d:operator]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("[e:or e:operator]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("f:\"phrase operator\""));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("g:`prox operator`"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+h:and +h:operator"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|I:sand |I:operator"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("-j:not -j:operator"));

    }
    
    @Test
    public void testMetaMultipleValues() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

        st.getQuestion().getRawInputParameters().put("meta_a", new String[] {"simple operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_a", new String[] {"simple operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_b_trunc", new String[] {"trunc operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_b_trunc", new String[] {"trunc operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_c_orplus", new String[] {"orplus operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_c_orplus", new String[] {"orplus operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_d_orsand", new String[] {"orsand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_d_orsand", new String[] {"orsand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_e_or", new String[] {"or operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_e_or", new String[] {"or operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_f_phrase", new String[] {"phrase \"operator\"", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_f_phrase", new String[] {"phrase \"operator\"", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_g_prox", new String[] {"prox operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_g_prox", new String[] {"prox operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_h_and", new String[] {"and operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_h_and", new String[] {"and operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_I_sand", new String[] {"sand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_I_sand", new String[] {"sand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("meta_j_not", new String[] {"not operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("smeta_j_not", new String[] {"not operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("dummy", new String[] {"value", "multiple"});
        st.getQuestion().getRawInputParameters().put("sdummy", new String[] {"value", "multiple"});

        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(10, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("a:simple a:operator a:multiple"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("b:*trunc* b:*operator* b:*multiple*"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[c:orplus c:operator c:multiple]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[d:orsand d:operator d:multiple]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[e:or e:operator e:multiple]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("f:\"phrase operator multiple\""));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("g:`prox operator multiple`"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+h:and +h:operator +h:multiple"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|I:sand |I:operator |I:multiple"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-j:not -j:operator -j:multiple"));
        
        Assert.assertEquals(10, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("a:simple a:operator a:multiple"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("b:*trunc* b:*operator* b:*multiple*"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+[c:orplus c:operator c:multiple]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|[d:orsand d:operator d:multiple]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("[e:or e:operator e:multiple]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("f:\"phrase operator multiple\""));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("g:`prox operator multiple`"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+h:and +h:operator +h:multiple"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|I:sand |I:operator |I:multiple"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("-j:not -j:operator -j:multiple"));

    }
    
    @Test
    public void testMetaSingleWord() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put("meta_a", new String[] {"simple"});
        st.getQuestion().getRawInputParameters().put("smeta_a", new String[] {"simple"});
        st.getQuestion().getRawInputParameters().put("meta_b_trunc", new String[] {"batman"});
        st.getQuestion().getRawInputParameters().put("smeta_b_trunc", new String[] {"batman"});
        st.getQuestion().getRawInputParameters().put("meta_c_orplus", new String[] {"spiderman"});
        st.getQuestion().getRawInputParameters().put("smeta_c_orplus", new String[] {"spiderman"});
        st.getQuestion().getRawInputParameters().put("meta_d_orsand", new String[] {"ironman"});
        st.getQuestion().getRawInputParameters().put("smeta_d_orsand", new String[] {"ironman"});
        st.getQuestion().getRawInputParameters().put("meta_e_or", new String[] {"elephantman"});
        st.getQuestion().getRawInputParameters().put("smeta_e_or", new String[] {"elephantman"});
        st.getQuestion().getRawInputParameters().put("meta_f_phrase", new String[] {"stallman"});        // Isn't he a super hero ? :)
        st.getQuestion().getRawInputParameters().put("smeta_f_phrase", new String[] {"stallman"});
        st.getQuestion().getRawInputParameters().put("meta_g_prox", new String[] {"superman"});
        st.getQuestion().getRawInputParameters().put("smeta_g_prox", new String[] {"superman"});
        st.getQuestion().getRawInputParameters().put("meta_h_and", new String[] {"hulk"});                // Ran out of *man
        st.getQuestion().getRawInputParameters().put("smeta_h_and", new String[] {"hulk"});
        st.getQuestion().getRawInputParameters().put("meta_i_sand", new String[] {"captainamerica"});    // Ran out of single words
        st.getQuestion().getRawInputParameters().put("smeta_i_sand", new String[] {"captainamerica"});
        st.getQuestion().getRawInputParameters().put("meta_j_not", new String[] {"silversurfer"});
        st.getQuestion().getRawInputParameters().put("smeta_j_not", new String[] {"silversurfer"});
        st.getQuestion().getRawInputParameters().put("dummy", new String[] {"value"});
        st.getQuestion().getRawInputParameters().put("sdummy", new String[] {"value"});
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(10, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("a:simple"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("b:*batman*"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[c:spiderman]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[d:ironman]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[e:elephantman]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("f:\"stallman\""));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("g:`superman`"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+h:hulk"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|i:captainamerica"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-j:silversurfer"));
        
        Assert.assertEquals(10, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("a:simple"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("b:*batman*"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+[c:spiderman]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|[d:ironman]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("[e:elephantman]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("f:\"stallman\""));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("g:`superman`"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+h:hulk"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|i:captainamerica"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("-j:silversurfer"));
    }
    
    @Test
    public void testQueryMultipleWords() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put("query_trunc", new String[] {"trunc operator"});
        st.getQuestion().getRawInputParameters().put("squery_trunc", new String[] {"trunc operator"});
        st.getQuestion().getRawInputParameters().put("query_orplus", new String[] {"orplus operator"});
        st.getQuestion().getRawInputParameters().put("squery_orplus", new String[] {"orplus operator"});
        st.getQuestion().getRawInputParameters().put("query_orsand", new String[] {"orsand operator"});
        st.getQuestion().getRawInputParameters().put("squery_orsand", new String[] {"orsand operator"});
        st.getQuestion().getRawInputParameters().put("query_or", new String[] {"or operator"});
        st.getQuestion().getRawInputParameters().put("squery_or", new String[] {"or operator"});
        st.getQuestion().getRawInputParameters().put("query_phrase", new String[] {"phrase operator"});
        st.getQuestion().getRawInputParameters().put("squery_phrase", new String[] {"phrase operator"});
        st.getQuestion().getRawInputParameters().put("query_prox", new String[] {"prox operator"});
        st.getQuestion().getRawInputParameters().put("squery_prox", new String[] {"prox operator"});
        st.getQuestion().getRawInputParameters().put("query_and", new String[] {"and operator"});
        st.getQuestion().getRawInputParameters().put("squery_and", new String[] {"and operator"});
        st.getQuestion().getRawInputParameters().put("query_sand", new String[] {"sand operator"});
        st.getQuestion().getRawInputParameters().put("squery_sand", new String[] {"sand operator"});
        st.getQuestion().getRawInputParameters().put("query_not", new String[] {"not operator"});
        st.getQuestion().getRawInputParameters().put("squery_not", new String[] {"not operator"});
        st.getQuestion().getRawInputParameters().put("dummy", new String[] {"value"});
        st.getQuestion().getRawInputParameters().put("sdummy", new String[] {"value"});
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(9, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("*trunc* *operator*"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[orplus operator]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[orsand operator]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[or operator]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("\"phrase operator\""));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("`prox operator`"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+and +operator"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|sand |operator"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-not -operator"));
        
        Assert.assertEquals(9, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("*trunc* *operator*"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+[orplus operator]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|[orsand operator]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("[or operator]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("\"phrase operator\""));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("`prox operator`"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+and +operator"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|sand |operator"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("-not -operator"));

    }
    
    @Test
    public void testQueryMultipleValues() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put("query_trunc", new String[] {"trunc operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_trunc", new String[] {"trunc operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_orplus", new String[] {"orplus operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_orplus", new String[] {"orplus operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_orsand", new String[] {"orsand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_orsand", new String[] {"orsand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_or", new String[] {"or operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_or", new String[] {"or operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_phrase", new String[] {"phrase operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_phrase", new String[] {"phrase operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_prox", new String[] {"prox operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_prox", new String[] {"prox operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_and", new String[] {"and operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_and", new String[] {"and operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_sand", new String[] {"sand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_sand", new String[] {"sand operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("query_not", new String[] {"not operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("squery_not", new String[] {"not operator", "multiple"});
        st.getQuestion().getRawInputParameters().put("dummy", new String[] {"value", "multiple"});
        st.getQuestion().getRawInputParameters().put("sdummy", new String[] {"value", "multiple"});
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(9, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("*trunc* *operator* *multiple*"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[orplus operator multiple]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[orsand operator multiple]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[or operator multiple]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("\"phrase operator multiple\""));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("`prox operator multiple`"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+and +operator +multiple"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|sand |operator |multiple"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-not -operator -multiple"));

        Assert.assertEquals(9, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("*trunc* *operator* *multiple*"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+[orplus operator multiple]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|[orsand operator multiple]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("[or operator multiple]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("\"phrase operator multiple\""));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("`prox operator multiple`"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+and +operator +multiple"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|sand |operator |multiple"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("-not -operator -multiple"));
    }


    @Test
    public void testQuerySingleWord() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put("query_trunc", new String[] {"batman"});
        st.getQuestion().getRawInputParameters().put("squery_trunc", new String[] {"batman"});
        st.getQuestion().getRawInputParameters().put("query_orplus", new String[] {"spiderman"});
        st.getQuestion().getRawInputParameters().put("squery_orplus", new String[] {"spiderman"});
        st.getQuestion().getRawInputParameters().put("query_orsand", new String[] {"ironman"});
        st.getQuestion().getRawInputParameters().put("squery_orsand", new String[] {"ironman"});
        st.getQuestion().getRawInputParameters().put("query_or", new String[] {"elephantman"});
        st.getQuestion().getRawInputParameters().put("squery_or", new String[] {"elephantman"});
        st.getQuestion().getRawInputParameters().put("query_phrase", new String[] {"stallman"});        // Isn't he a super hero ? :)
        st.getQuestion().getRawInputParameters().put("squery_phrase", new String[] {"stallman"});
        st.getQuestion().getRawInputParameters().put("query_prox", new String[] {"superman"});
        st.getQuestion().getRawInputParameters().put("squery_prox", new String[] {"superman"});
        st.getQuestion().getRawInputParameters().put("query_and", new String[] {"hulk"});                // Ran out of *man
        st.getQuestion().getRawInputParameters().put("squery_and", new String[] {"hulk"});
        st.getQuestion().getRawInputParameters().put("query_sand", new String[] {"captainamerica"});    // Ran out of single words
        st.getQuestion().getRawInputParameters().put("squery_sand", new String[] {"captainamerica"});
        st.getQuestion().getRawInputParameters().put("query_not", new String[] {"silversurfer"});
        st.getQuestion().getRawInputParameters().put("squery_not", new String[] {"silversurfer"});
        st.getQuestion().getRawInputParameters().put("dummy", new String[] {"value"});
        st.getQuestion().getRawInputParameters().put("sdummy", new String[] {"value"});
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(9, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("*batman*"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+[spiderman]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|[ironman]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[elephantman]"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("\"stallman\""));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("`superman`"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("+hulk"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|captainamerica"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("-silversurfer"));

        Assert.assertEquals(9, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("*batman*"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+[spiderman]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|[ironman]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("[elephantman]"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("\"stallman\""));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("`superman`"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("+hulk"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|captainamerica"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("-silversurfer"));

    }
    
    @Test
    public void testInvalidParameters() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

        st.getQuestion().getRawInputParameters().put("meta_", new String[] {"incomplete"});
        st.getQuestion().getRawInputParameters().put("smeta_", new String[] {"incomplete"});
        st.getQuestion().getRawInputParameters().put("meta_x_invalid", new String[] {"second bad"});    
        st.getQuestion().getRawInputParameters().put("smeta_x_invalid", new String[] {"second bad"});    
        st.getQuestion().getRawInputParameters().put("meta_x_", new String[] {"incomplete too"});
        st.getQuestion().getRawInputParameters().put("smeta_x_", new String[] {"incomplete too"});
        st.getQuestion().getRawInputParameters().put("query_invalid", new String[] {"first bad"});
        st.getQuestion().getRawInputParameters().put("squery_invalid", new String[] {"first bad"});
        st.getQuestion().getRawInputParameters().put("query_", new String[] {"incomplete"});
        st.getQuestion().getRawInputParameters().put("squery_", new String[] {"incomplete"});
        st.getQuestion().getRawInputParameters().put("meta_invalid", new String[] {"abc"});
        st.getQuestion().getRawInputParameters().put("smeta_invalid", new String[] {"abc"});
        st.getQuestion().getRawInputParameters().put("meta_inv_or", new String[] {"def"});
        st.getQuestion().getRawInputParameters().put("smeta_inv_or", new String[] {"def"});
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(7, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("incomplete"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("x:second x:bad"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("x:incomplete x:too"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("first bad"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("incomplete"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("abc"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("[def]"));
        
        Assert.assertEquals(7, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("incomplete"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("x:second x:bad"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("x:incomplete x:too"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("first bad"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("incomplete"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("abc"));
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("[def]"));

    }
    
    @Test
    public void testCombination() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);

        st.getQuestion().getRawInputParameters().put("meta_Z_phrase_sand", new String[] {"incomplete"});
        st.getQuestion().getRawInputParameters().put("smeta_Z_phrase_sand", new String[] {"incomplete"});
        
        MetaParameters processor = new MetaParameters();
        processor.processInput(st);
        
        Assert.assertEquals(1, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("|Z:\"incomplete\""));

        Assert.assertEquals(1, st.getQuestion().getSystemMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getSystemMetaParameters().contains("|Z:\"incomplete\""));
    }
    
    @Test
    public void testMetaDatesAreSkipped() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put("meta_d1day", new String[] {"1day"});
        st.getQuestion().getRawInputParameters().put("smeta_d1day", new String[] {"1day"});
        st.getQuestion().getRawInputParameters().put("meta_d2month", new String[] {"2month"});
        st.getQuestion().getRawInputParameters().put("smeta_d2month", new String[] {"2month"});
        st.getQuestion().getRawInputParameters().put("meta_d3year", new String[] {"3year"});
        st.getQuestion().getRawInputParameters().put("smeta_d3year", new String[] {"3year"});
        st.getQuestion().getRawInputParameters().put("meta_wday", new String[] {"wday"});
        st.getQuestion().getRawInputParameters().put("smeta_wday", new String[] {"wday"});
        st.getQuestion().getRawInputParameters().put("meta_xmonth", new String[] {"xmonth"});
        st.getQuestion().getRawInputParameters().put("smeta_xmonth", new String[] {"xmonth"});
        st.getQuestion().getRawInputParameters().put("meta_yyear", new String[] {"yyear"});
        st.getQuestion().getRawInputParameters().put("smeta_yyear", new String[] {"yyear"});
        
        new MetaParameters().processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getMetaParameters().size());
        Assert.assertEquals(0, st.getQuestion().getSystemMetaParameters().size());
    }
    
    /**
     * FUN-6235
     */
    @Test
    public void testEventSearchParamsDontConflict() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put("meta_a", new String[] {"value_a"});
        st.getQuestion().getRawInputParameters().put("meta_w", new String[] {"value_w"});
        st.getQuestion().getRawInputParameters().put("meta_x", new String[] {"value_x"});
        st.getQuestion().getRawInputParameters().put("meta_y", new String[] {"value_y"});
        st.getQuestion().getRawInputParameters().put("meta_z", new String[] {"value_z"});
        
        new MetaParameters().processInput(st);
        
        Assert.assertEquals(5, st.getQuestion().getMetaParameters().size());
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("a:value_a"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("w:value_w"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("x:value_x"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("y:value_y"));
        Assert.assertTrue(st.getQuestion().getMetaParameters().contains("z:value_z"));

    }
    
}

