package com.funnelback.publicui.knowledgegraph.model;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class KnowledgeGraphLabelsTest {

    @Test
    public void testFromConfigFile() throws IOException, InvalidInputException {
        ByteArrayInputStream bias = new ByteArrayInputStream((
            "[ {\n"
                + "  \"created\" : \"2018-09-12T12:04:22.519+10:00\",\n"
                + "  \"lastModified\" : \"2018-10-22T16:35:49.379+11:00\",\n"
                + "  \"id\" : \"cde16db2-8690-40fa-a172-0e0bca0fdf69\",\n"
                + "  \"category\" : \"METADATA\",\n"
                + "  \"key\" : \"author\",\n"
                + "  \"label\" : \"Author\",\n"
                + "  \"type\" : \"doc\"\n"
                + "}, {\n"
                + "  \"created\" : \"2018-09-12T12:04:22.519+10:00\",\n"
                + "  \"lastModified\" : \"2018-10-22T16:35:49.379+11:00\",\n"
                + "  \"id\" : \"cde16db2-8690-40fa-a172-0e0bca0fdf69\",\n"
                + "  \"category\" : \"METADATA\",\n"
                + "  \"key\" : \"keyValue\",\n"
                + "  \"label\" : \"labelValue\",\n"
                + "  \"type\" : \"typeValue\"\n"
                + "}, {\n"
                + "  \"created\" : \"2018-09-12T12:04:22.519+10:00\",\n"
                + "  \"lastModified\" : \"2018-10-22T16:35:49.379+11:00\",\n"
                + "  \"id\" : \"cde16db2-8690-40fa-a172-0e0bca0fdf69\",\n"
                + "  \"category\" : \"RELATIONSHIP\",\n"
                + "  \"key\" : \"keyValueR\",\n"
                + "  \"label\" : \"labelValueR\"\n"
                + "}, {\n"
                + "  \"created\" : \"2018-09-12T12:04:22.519+10:00\",\n"
                + "  \"lastModified\" : \"2018-10-22T16:35:49.379+11:00\",\n"
                + "  \"id\" : \"cde16db2-8690-40fa-a172-0e0bca0fdf69\",\n"
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

    @Test(expected = MismatchedInputException.class)
    public void testEmptyInput() throws IOException {
        ByteArrayInputStream bias = new ByteArrayInputStream(("").getBytes());

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
}