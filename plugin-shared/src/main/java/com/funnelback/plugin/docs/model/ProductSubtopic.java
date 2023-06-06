package com.funnelback.plugin.docs.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * List of subtopic types, see {@link ProductSubtopicCategory}
 */
public class ProductSubtopic {

    /**
     * "Search data sources" product topic
     */
    @RequiredArgsConstructor
    public enum DataSources implements ProductSubtopicCategory {
        CUSTOM("Custom"),
        DATABASE("Database"),
        DIRECTORY("Directory (LDAP/AD)"),
        SOCIAL_MEDIA("Social media"),
        WEB("Web"),
        DELIMITED_TEXT("Delimited text (CSV/TSV)"),
        JSON("JSON"),
        XML("XML"),
        FILE_SYSTEM("File system"),
        SQUIZ_CONNECT("Squiz Connect"),
        SQUIZ_MATRIX("Squiz Matrix");

        private final String topic;

        @JsonValue
        public String getTopic() {
            return topic;
        }
    }

    /**
     * "Search results pages" product topic
     */
    @RequiredArgsConstructor
    public enum ResultsPage implements  ProductSubtopicCategory {
        AUTO_COMPLETION("Auto-completion"),
        BEST_BETS("Best bets"),
        CACHED_RESULTS("Cached results"),
        CONTEXTUAL_NAVIGATION("Contextual navigation"),
        CURATOR("Curator"),
        DATA_MODEL_MANIPULATION("Data model manipulation"),
        DATA_MODEL_SEARCH("Search data model"),
        DLS("DLS (DOCUMENT LEVEL SECURITY)"),
        EVENT_SEARCH("Event search"),
        EXTRA_SEARCHES("Extra searches"),
        FACETED_NAVIGATION("Faceted navigation"),
        GEOSPATIAL_SEARCH("Geospatial search"),
        LOCALIZATION("Localization"),
        PERSONALIZATION("Personalization"),
        QUERY_LANGUAGE("Query language"),
        RESULTS_TEMPLATES("Search results and templates"),
        SESSIONS_HISTORY("Search sessions and history");

        private final String topic;

        @JsonValue
        public String getTopic() {
            return topic;
        }
    }

    /**
     * "Analytics and reporting" product topic
     */
    @RequiredArgsConstructor
    public enum AnalyticsReporting implements ProductSubtopicCategory {
        ANALYTICS_USAGE("Search usage analytics"),
        ACCESSIBILITY_REPORTING("Accessibility reporting"),
        CONTENT_REPORTING("Content reporting"),
        SEARCH_TRENDS("Search trends"),
        DATA_REPORTING("Data reporting");

        private final String topic;

        @JsonValue
        public String getTopic() {
            return topic;
        }
    }

    /**
     * "Application administration" product topic
     */
    @RequiredArgsConstructor
    public enum ApplicationAdministration implements ProductSubtopicCategory {
        ADMINISTRATION_DASHBOARD("Administration dashboard"),
        USER_MANAGEMENT("User management");

        private final String topic;

        @JsonValue
        public String getTopic() {
            return topic;
        }
    }

    /**
     * "Search indexing" product  topic
     */
    @RequiredArgsConstructor
    public enum Indexing implements ProductSubtopicCategory {
        DOCUMENT_FILTERS("Document filters"),
        INDEX_MANIPULATION("Index manipulation"),
        KNOWLEDGE_GRAPH("Knowledge graph"),
        METADATA("Metadata"),
        PUSH_INDEXES("Push indexes"),
        SPELLING_SUGGESTIONS("Spelling suggestions"),
        WORKFLOW_SCRIPTS("Workflow scripts");

        private final String topic;

        @JsonValue
        public String getTopic() {
            return topic;
        }
    }

    /**
     * "Integration and development" product topic
     */
    @RequiredArgsConstructor
    public enum IntegrationDevelopment implements ProductSubtopicCategory {
        API("APIs"),
        PERFORMANCE("Performance"),
        PLUGIN("Plugins");

        private final String topic;

        @JsonValue
        public String getTopic() {
            return topic;
        }
    }

    /**
     * "Search ranking and sorting" product topic
     */
    @RequiredArgsConstructor
    public enum RankingSorting implements ProductSubtopicCategory {
        PADRE("Padre"),
        QIE("Query independent evidence"),
        QUERY_BLENDING("Query blending"),
        QUERY_OPTIMIZATION("Query optimization"),
        RANKING_SORTING("Result ranking and sorting"),
        RESULT_COLLAPSING("Result collapsing"),
        RESULT_DIVERSIFICATION("Result diversification"),
        SEO_TUNING("SEO and tuning"),
        STEMMING("Stemming"),
        SYNONYMS("Synonyms");

        private final String topic;

        @JsonValue
        public String getTopic() {
            return topic;
        }
    }

    /**
     * "System administration" product topic
     */
    @RequiredArgsConstructor
    public enum SystemAdministration implements ProductSubtopicCategory {
        BACKUP("Backup"),
        GLOBAL_CONFIGURATION("Global configuration"),
        INSTALLATION("Installation"),
        LOGGING("Logging"),
        MONITORING("Monitoring"),
        MULTI_SERVER_CONFIGURATION("Multi-server configuration"),
        PATCHING("Patching"),
        SECURITY("Security"),
        SYSTEM_SERVICES("System services"),
        UPGRADES("Upgrades");

        @Getter
        private final String topic;

    }
}
