package com.funnelback.publicui.search.model.transaction.testutils;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class SearchQuestionTestHelperTest {

    @Test
    public void setProfileConfigOptionsTest() {
        SearchTransaction transaction = new SearchTransaction();
        Map<String, String> profileConfig = new HashMap<>();
        SearchQuestionTestHelper.setCurrentProfileConfig(profileConfig, transaction.getQuestion());

        profileConfig.put("foo", "bar");
        profileConfig.put("a", "b");

        Assertions.assertEquals("bar", transaction.getQuestion().getCurrentProfileConfig().get("foo"));
        Assertions.assertTrue(transaction.getQuestion().getCurrentProfileConfig().getRawKeys().contains("foo"));
        Assertions.assertTrue(transaction.getQuestion().getCurrentProfileConfig().getRawKeys().contains("a"));
    }
}