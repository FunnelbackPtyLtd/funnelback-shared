package com.funnelback.publicui.knowledgegraph.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
public class KnowledgeGraphTemplate {
    private String icon = "";
    private String title = "";
    private String subtitle = "";
    private String desc = "";
    private final PrimaryAndSecondaryLists list = new PrimaryAndSecondaryLists();
    private final PrimaryAndSecondaryLists detail = new PrimaryAndSecondaryLists();

    @Data
    public static class PrimaryAndSecondaryLists {
        private final List<String> primary = new ArrayList<>();
        private final List<String> secondary = new ArrayList<>();
    }

    public static Map<String, KnowledgeGraphTemplate> fromConfigFile(InputStream is) throws IOException, InvalidInputException {
        Map<String, KnowledgeGraphTemplate> result = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(is);

        if (json == null) {
            throw new InvalidInputException("Input data was empty, top level array required.");
        }

        if (json.isArray()) {
            json.forEach((entry) -> {
                Optional<String> firstValidationError = validateEntry(entry);
                if (firstValidationError.isPresent()) {
                    throw new InvalidInputException(firstValidationError.get());
                }

                String type = entry.get("type").textValue();

                KnowledgeGraphTemplate template = new KnowledgeGraphTemplate();
                template.setIcon(entry.get("icon").textValue());
                template.setTitle(entry.get("title").textValue());
                template.setSubtitle(entry.get("subtitle").textValue());
                template.setDesc(entry.get("desc").textValue());

                entry.get("list").get("primary").forEach(item -> template.getList().getPrimary().add(item.textValue()));
                entry.get("list").get("secondary").forEach(item -> template.getList().getSecondary().add(item.textValue()));
                entry.get("detail").get("primary").forEach(item -> template.getDetail().getPrimary().add(item.textValue()));
                entry.get("detail").get("secondary").forEach(item -> template.getDetail().getSecondary().add(item.textValue()));

                result.put(type, template);
            });
        } else {
            throw new InvalidInputException("Invalid input (should have a top-level array)");
        }

        return result;
    }

    private static Set<String> requiredFields = ImmutableSet.of("type","icon","title","subtitle","desc","list","detail");
    private static Optional<String> validateEntry(JsonNode entry) {
        for (String requiredField : requiredFields) {
            if (!entry.hasNonNull(requiredField)) {
                return Optional.of("Entry is missing required field " + requiredField);
            }
        }

        Optional<String> listError = validatePrimaryAndSecondaryLists(entry.get("list"), "list");
        if (listError.isPresent()) {
            return listError;
        }

        Optional<String> detailError = validatePrimaryAndSecondaryLists(entry.get("detail"), "detail");
        if (detailError.isPresent()) {
            return detailError;
        }

        return Optional.empty();
    }

    private static Optional<String> validatePrimaryAndSecondaryLists(JsonNode entry, String nameForMessages) {
        if (!entry.isObject()) {
            return Optional.of("Entry " + nameForMessages + " should be an object but was not");
        }
        if (!entry.hasNonNull("primary") || !entry.get("primary").isArray()) {
            return Optional.of("Entry " + nameForMessages + ".primary should be present and an array");
        }
        if (!entry.hasNonNull("secondary") || !entry.get("secondary").isArray()) {
            return Optional.of("Entry " + nameForMessages + ".secondary should be present and an array");
        }

        return Optional.empty();
    }
}
