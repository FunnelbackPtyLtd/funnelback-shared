package com.funnelback.publicui.knowledgegraph.model;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class KnowledgeGraphTemplateTest {

    @Test
    public void testFromConfigFile() throws IOException, InvalidInputException {
        ByteArrayInputStream bias = new ByteArrayInputStream((
                  "[ {\n"
                + "  \"created\" : \"2018-09-12T12:04:22.519+10:00\",\n"
                + "  \"lastModified\" : \"2018-10-22T16:35:49.379+11:00\",\n"
                + "  \"id\" : \"cde16db2-8690-40fa-a172-0e0bca0fdf69\",\n"
                + "  \"type\" : \"document\",\n"
                + "  \"icon\" : \"fa-doc\",\n"
                + "  \"title\" : \"author\",\n"
                + "  \"subtitle\" : \"category\",\n"
                + "  \"desc\" : \"some description\",\n"
                + "  \"list\" : {\n"
                + "    \"primary\" : [ \"size\" ],\n"
                + "    \"secondary\" : [ \"created\" ]\n"
                + "  },\n"
                + "  \"detail\" : {\n"
                + "    \"primary\" : [ \"size2\" ],\n"
                + "    \"secondary\" : [ \"created2\" ]\n"
                + "  },\n"
                + "  \"sort\": {\n"
                + "    \"field\": \"field\",\n"
                + "    \"order\": \"ASC\"\n"
                + "  }"
                + "} ]").getBytes());

        Map<String, KnowledgeGraphTemplate> templates = KnowledgeGraphTemplate.fromConfigFile(bias);

        Assert.assertEquals(templates.size(), 1);
        Assert.assertEquals(templates.get("document").getIcon(), "fa-doc");
        Assert.assertEquals(templates.get("document").getTitle(), "author");
        Assert.assertEquals(templates.get("document").getSubtitle(), "category");
        Assert.assertEquals(templates.get("document").getDesc(), "some description");
        Assert.assertEquals(templates.get("document").getSort().getField(), "field");
        Assert.assertEquals(templates.get("document").getSort().getOrder(), KnowledgeGraphTemplate.SortOrder.ASC);
        Assert.assertEquals(templates.get("document").getList().getPrimary(), ImmutableList.of("size"));
        Assert.assertEquals(templates.get("document").getList().getSecondary(), ImmutableList.of("created"));
        Assert.assertEquals(templates.get("document").getDetail().getPrimary(), ImmutableList.of("size2"));
        Assert.assertEquals(templates.get("document").getDetail().getSecondary(), ImmutableList.of("created2"));
    }

    @Test(expected = MismatchedInputException.class)
    public void testEmptyInput() throws IOException {
        ByteArrayInputStream bias = new ByteArrayInputStream(("").getBytes());

        Map<String, KnowledgeGraphTemplate> templates = KnowledgeGraphTemplate.fromConfigFile(bias);
    }

    @Test(expected = MismatchedInputException.class)
    public void testNonArrayInput() throws IOException {
        ByteArrayInputStream bias = new ByteArrayInputStream(("{}").getBytes());

        Map<String, KnowledgeGraphTemplate> templates = KnowledgeGraphTemplate.fromConfigFile(bias);
    }

    @Test
    public void testEmptyArrayInput() throws IOException {
        ByteArrayInputStream bias = new ByteArrayInputStream(("[]").getBytes());

        Map<String, KnowledgeGraphTemplate> templates = KnowledgeGraphTemplate.fromConfigFile(bias);

        Assert.assertEquals(templates.size(), 0);
    }
}