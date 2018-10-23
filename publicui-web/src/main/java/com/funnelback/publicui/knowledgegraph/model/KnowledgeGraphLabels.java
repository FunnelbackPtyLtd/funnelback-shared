package com.funnelback.publicui.knowledgegraph.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(is);

        if (json == null) {
            throw new InvalidInputException("Input data was empty, top level array required.");
        }

        if (json.isArray()) {
            json.forEach((entry) -> {
                if (!(entry.hasNonNull("category") &&
                    entry.hasNonNull("key") &&
                    entry.hasNonNull("label"))) {
                    throw new InvalidInputException("Input data missing required properties (category, key and label).");
                }

                String category = entry.get("category").textValue();
                String key = entry.get("key").textValue();
                String label = entry.get("label").textValue();

                if (category.equals("PROPERTY")) {
                    if (!entry.hasNonNull("type")) {
                        throw new InvalidInputException("Missing required property 'type' for PROPERTY entry.");
                    }

                    String type = entry.get("type").textValue();

                    if (!result.getProperty().containsKey(type)) {
                        result.getProperty().put(type, new HashMap<>());
                    }

                    result.getProperty().get(type).put(key, label);
                } else if (category.equals("RELATIONSHIP")) {
                    result.getRelationship().put(key, label);
                } else if (category.equals("TYPE")) {
                    result.getType().put(key, label);
                } else {
                    throw new InvalidInputException("Unknown category type " + category);
                }

            });
        } else {
            throw new InvalidInputException("Invalid input (should have a top-level array)");
        }

        return result;
    }
}
