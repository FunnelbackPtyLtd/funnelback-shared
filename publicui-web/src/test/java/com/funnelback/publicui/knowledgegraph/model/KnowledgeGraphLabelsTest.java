package com.funnelback.publicui.knowledgegraph.model;

import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class KnowledgeGraphLabelsTest {

    @Test
    public void testFromConfigFile() throws IOException, InvalidInputException {
        ByteArrayInputStream bias = new ByteArrayInputStream((
                "[ {\n"
              + "  \"created\" : \"should\",\n"
              + "  \"lastModified\" : \"be\",\n"
              + "  \"id\" : \"ignored\",\n"
              + "  \"category\" : \"PROPERTY\",\n"
              + "  \"key\" : \"author\",\n"
              + "  \"label\" : \"Author\",\n"
              + "  \"type\" : \"doc\"\n"
              + "}, {\n"
              + "  \"category\" : \"PROPERTY\",\n"
              + "  \"key\" : \"keyValue\",\n"
              + "  \"label\" : \"labelValue\",\n"
              + "  \"type\" : \"typeValue\"\n"
              + "}, {\n"
              + "  \"category\" : \"RELATIONSHIP\",\n"
              + "  \"key\" : \"keyValueR\",\n"
              + "  \"label\" : \"labelValueR\"\n"
              + "}, {\n"
              + "  \"category\" : \"TYPE\",\n"
              + "  \"key\" : \"keyValueT\",\n"
              + "  \"label\" : \"labelValueT\"\n"
              + "} ]").getBytes());

        KnowledgeGraphLabels labels = KnowledgeGraphLabels.fromConfigFile(bias);

        Assert.assertEquals(labels.getProperty().size(), 2);
        Assert.assertEquals(labels.getProperty().get("doc"), ImmutableMap.of("author","Author"));
        Assert.assertEquals(labels.getProperty().get("typeValue"), ImmutableMap.of("keyValue","labelValue"));

        Assert.assertEquals(labels.getRelationship(), ImmutableMap.of("keyValueR","labelValueR"));
        Assert.assertEquals(labels.getType(), ImmutableMap.of("keyValueT","labelValueT"));
    }

    @Test(expected = InvalidInputException.class)
    public void testEmptyInput() throws IOException, InvalidInputException {
        ByteArrayInputStream bias = new ByteArrayInputStream(("").getBytes());

        KnowledgeGraphLabels labels = KnowledgeGraphLabels.fromConfigFile(bias);
    }

    @Test(expected = InvalidInputException.class)
    public void testNonArrayInput() throws IOException, InvalidInputException {
        ByteArrayInputStream bias = new ByteArrayInputStream(("{}").getBytes());

        KnowledgeGraphLabels labels = KnowledgeGraphLabels.fromConfigFile(bias);
    }

    @Test
    public void testEmptyArrayInput() throws IOException, InvalidInputException {
        ByteArrayInputStream bias = new ByteArrayInputStream(("[]").getBytes());

        KnowledgeGraphLabels labels = KnowledgeGraphLabels.fromConfigFile(bias);

        Assert.assertEquals(labels.getProperty().size(), 0);
        Assert.assertEquals(labels.getRelationship().size(), 0);
        Assert.assertEquals(labels.getType().size(), 0);
    }

    @Test(expected = InvalidInputException.class)
    public void testMissingProperties() throws IOException, InvalidInputException {
        ByteArrayInputStream bias = new ByteArrayInputStream((
                  "[ {\n"
                + "  \"created\" : \"should\",\n"
                + "  \"lastModified\" : \"be\",\n"
                + "  \"id\" : \"ignored\",\n"
                + "  \"category\" : \"PROPERTY\",\n"
                + "  \"labelIsMissing\" : \"Author\",\n"
                + "  \"type\" : \"doc\"\n"
                + "} ]").getBytes());

        KnowledgeGraphLabels labels = KnowledgeGraphLabels.fromConfigFile(bias);
    }

}