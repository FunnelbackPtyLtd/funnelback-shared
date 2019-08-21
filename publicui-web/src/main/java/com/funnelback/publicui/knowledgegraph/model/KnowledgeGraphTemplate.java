package com.funnelback.publicui.knowledgegraph.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.funnelback.adminapi.client.model.KnowledgeGraphTemplateModel;
import com.funnelback.publicui.knowledgegraph.exception.InvalidInputException;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class KnowledgeGraphTemplate {
    private String icon = "";
    private String title = "";
    private String subtitle = "";
    private String desc = "";
    private String image = "";
    private ImageDefault imageDefault;
    private final PrimaryAndSecondaryLists list = new PrimaryAndSecondaryLists();
    private final PrimaryAndSecondaryLists detail = new PrimaryAndSecondaryLists();

    private SortList sort;

    @Data
    public static class PrimaryAndSecondaryLists {
        private final List<String> primary = new ArrayList<>();
        private final List<String> secondary = new ArrayList<>();
    }

    @Data
    public static class ImageDefault {
        private final String value;
        private final ImageDefaultType type;
    }

    @Data
    public static class SortList {
        private final String field;
        private final SortOrder order;
        private final Boolean group;
    }

    public enum ImageDefaultType {
        ICON,
        URL;
    }

    public enum SortOrder {
        ASC,
        DESC;
    }

    public static Map<String, KnowledgeGraphTemplate> fromConfigFile(InputStream is) throws IOException, InvalidInputException {
        Map<String, KnowledgeGraphTemplate> result = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.registerModule(new JavaTimeModule());

        List<KnowledgeGraphTemplateModel> configTemplates = mapper.readValue(is, new TypeReference<List<KnowledgeGraphTemplateModel>>(){});

        for (KnowledgeGraphTemplateModel configTemplate : configTemplates) {
            KnowledgeGraphTemplate template = new KnowledgeGraphTemplate();
            template.setIcon(configTemplate.getIcon());
            template.setTitle(configTemplate.getTitle());
            template.setSubtitle(configTemplate.getSubtitle());
            template.setDesc(configTemplate.getDesc());
            template.setImage(configTemplate.getImage());

            template.setImageDefault(new ImageDefault(
                configTemplate.getImageDefault().getValue(),
                ImageDefaultType.valueOf(configTemplate.getImageDefault().getType().getValue())
            ));

            template.setSort(
                new SortList(
                    configTemplate.getSort().getField(),
                    SortOrder.valueOf(configTemplate.getSort().getOrder().getValue()),
                    configTemplate.getSort().isGroup()
                )
            );

            template.getList().getPrimary().addAll(configTemplate.getList().getPrimary());
            template.getList().getSecondary().addAll(configTemplate.getList().getSecondary());
            template.getDetail().getPrimary().addAll(configTemplate.getDetail().getPrimary());
            template.getDetail().getSecondary().addAll(configTemplate.getDetail().getSecondary());

            result.put(configTemplate.getType(), template);
        }

        return result;
    }
}
