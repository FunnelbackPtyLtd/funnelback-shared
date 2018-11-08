package com.funnelback.publicui.knowledgegraph.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.funnelback.adminapi.client.model.KnowledgeGraphLabelModel;
import com.funnelback.adminapi.client.model.KnowledgeGraphTemplateModel;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class KnowledgeGraphLabels {
    private final Map<String, Map<String, String>> property = new HashMap<>();
    private final Map<String, String> relationship = new HashMap<>();
    private final Map<String, String> type = new HashMap<>();

    public static KnowledgeGraphLabels fromConfigFile(InputStream is) throws IOException, InvalidInputException {
        KnowledgeGraphLabels result = new KnowledgeGraphLabels();

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.registerModule(new JavaTimeModule());

        List<KnowledgeGraphLabelModel> configLabels = mapper.readValue(is, new TypeReference<List<KnowledgeGraphLabelModel>>(){});

        for (KnowledgeGraphLabelModel configLabel : configLabels) {
            if (configLabel.getCategory().equals(KnowledgeGraphLabelModel.CategoryEnum.METADATA)) {
                result.getProperty().putIfAbsent(configLabel.getType(), new HashMap<>());
                result.getProperty().get(configLabel.getType()).put(configLabel.getKey(), configLabel.getLabel());
            } else if (configLabel.getCategory().equals(KnowledgeGraphLabelModel.CategoryEnum.RELATIONSHIP)) {
                result.getRelationship().put(configLabel.getKey(), configLabel.getLabel());
            } else if (configLabel.getCategory().equals(KnowledgeGraphLabelModel.CategoryEnum.TYPE)) {
                result.getType().put(configLabel.getKey(), configLabel.getLabel());
            } else {
                throw new RuntimeException("Unknown category type " + configLabel.getCategory().toString());
            }
        }

        return result;
    }
}
