package com.funnelback.publicui.knowledgegraph.model;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.knowledgegraph.labels.GenericKnowledgeGraphLabelsMarshaller;
import com.funnelback.common.knowledgegraph.labels.KnowledgeGraphLabel;
import com.google.common.collect.ImmutableMap;

public class KnowledgeGraphLabelsModelTest {

    private GenericKnowledgeGraphLabelsMarshaller marshaller = new GenericKnowledgeGraphLabelsMarshaller();

    @Test
    public void testFromConfigFile() throws RuntimeException {
        List<KnowledgeGraphLabel> labels = marshaller.unMarshal(Optional.of(V1_JSON_LABELS.getBytes()), null);
        KnowledgeGraphLabelsModel result = KnowledgeGraphLabelsModel.fromConfigFile(labels);

        Assert.assertEquals(result.getProperty().size(), 2);
        Assert.assertEquals(result.getProperty().get("doc"), ImmutableMap.of("author", "Author"));
        Assert.assertEquals(result.getProperty().get("typeValue"), ImmutableMap.of("keyValue", "labelValue"));
        Assert.assertEquals(result.getRelationship(), ImmutableMap.of("keyValueR", "labelValueR"));
        Assert.assertEquals(result.getType(), ImmutableMap.of("keyValueT", "labelValueT"));
    }

    @Test
    public void testEmptyArrayInput() {
        List<KnowledgeGraphLabel> labels = marshaller.unMarshal(Optional.of("[]".getBytes()), null);
        KnowledgeGraphLabelsModel result = KnowledgeGraphLabelsModel.fromConfigFile(labels);

        Assert.assertEquals(result.getProperty().size(), 0);
        Assert.assertEquals(result.getRelationship().size(), 0);
        Assert.assertEquals(result.getType().size(), 0);
    }
    
    String V1_JSON_LABELS = "[ {\n"
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
        + "} ]";

}
