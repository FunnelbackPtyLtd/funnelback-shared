package com.funnelback.publicui.knowledgegraph.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.funnelback.common.knowledgegraph.labels.KnowledgeGraphLabel;

import lombok.Data;

/**
 * Convert knowledge graph labels model (saved by admin API) into model expected by knowledge graph widget.
 * Labels are grouped by categories (`LabelCategory`) and properties are extra grouped by `type`.
 * Labels are returned in format `key = label` where `key` is a key to be translated
 * and `label` is a human friendly translation of the key. For example:
 * {
 *    property: {
 *      movie: {starring: "Cast", genre: "Genre"},
 *      superhero: {alias: "Nickname", own: "Characteristic"},
 *    },
 *    relationship: {actedIn.outgoing: "Acted in", actedIn.incoming: "Actors"},
 *    type: {movie: "Movie", superhero: "Super hero"}
 * }
 */
@Data
public class KnowledgeGraphLabelsModel {

    private final Map<String, Map<String, String>> property = new HashMap<>();
    private final Map<String, String> relationship = new HashMap<>();
    private final Map<String, String> type = new HashMap<>();

    public static KnowledgeGraphLabelsModel fromConfigFile(List<KnowledgeGraphLabel> labels) throws RuntimeException {
        KnowledgeGraphLabelsModel result = new KnowledgeGraphLabelsModel();
        for (KnowledgeGraphLabel label : labels) {
            if (label.getCategory().equals(KnowledgeGraphLabel.LabelCategory.METADATA)) {
                result.getProperty().putIfAbsent(label.getType(), new HashMap<>());
                result.getProperty().get(label.getType()).put(label.getKey(), label.getLabel());
            } else if (label.getCategory().equals(KnowledgeGraphLabel.LabelCategory.RELATIONSHIP)) {
                result.getRelationship().put(label.getKey(), label.getLabel());
            } else if (label.getCategory().equals(KnowledgeGraphLabel.LabelCategory.TYPE)) {
                result.getType().put(label.getKey(), label.getLabel());
            } else {
                throw new RuntimeException("Unknown category type " + label.getCategory().toString());
            }
        }
        return result;
    }
}
