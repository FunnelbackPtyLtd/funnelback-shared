package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import lombok.extern.log4j.Log4j;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.funnelback.publicui.search.model.curator.config.CuratorConfig;

/**
 * Parses a curator config file and returns a CuratorConfig object.
 * 
 */
@Log4j
public class CuratorConifgResource extends AbstractSingleFileResource<CuratorConfig> {

    /**
     * Create the CuratorConifgResource with a file object representing the
     * config file to be parsed.
     */
    public CuratorConifgResource(File configFile) {
        super(configFile);
    }

    /**
     * Parse the curator config file and return a CuratorConfig object based on
     * it.
     */
    @Override
    public CuratorConfig parse() throws IOException {
        log.debug("Reading curator configuration data from '" + file.getAbsolutePath() + "'");

        return loadYamlConfig(file.getAbsolutePath());
    }

    /** Trigger classes to add shortName aliases for (to keep the config simple) */
    public static final Class<?>[] aliasedTriggers = new Class<?>[] {
                    com.funnelback.curator.trigger.AllQueryWordsTrigger.class,
                    com.funnelback.curator.trigger.AndTrigger.class,
                    com.funnelback.curator.trigger.CountryNameTrigger.class,
                    com.funnelback.curator.trigger.DateRangeTrigger.class,
                    com.funnelback.curator.trigger.ExactQueryTrigger.class,
                    com.funnelback.curator.trigger.OrTrigger.class,
                    com.funnelback.curator.trigger.QueryRegularExpressionTrigger.class,
                    com.funnelback.curator.trigger.QuerySubstringTrigger.class };
    /** Action classes to add shortName aliases for (to keep the config simple) */
    public static final Class<?>[] aliasedActions = new Class<?>[] {
                    com.funnelback.curator.action.DisplayMessage.class,
                    com.funnelback.curator.action.DisplayUrlAdvert.class,
                    com.funnelback.curator.action.PromoteUrls.class, com.funnelback.curator.action.RemoveUrls.class };

    /**
     * Get a snakeyaml parsing/serializing object which is already configured
     * with aliases for the relevant Action and Trigger classes.
     */
    public static Yaml getYamlObject() {
        Constructor constructor = new Constructor();
        Representer representer = new Representer();

        for (Class<?> clazz : aliasedTriggers) {
            String name = clazz.getSimpleName().replaceAll("Trigger$", "");
            addAlias(clazz, name, constructor, representer);
        }

        for (Class<?> clazz : aliasedActions) {
            addAlias(clazz, clazz.getSimpleName(), constructor, representer);
        }

        Yaml yaml = new Yaml(constructor, representer, new DumperOptions());
        return yaml;
    }

    /**
     * Add an alias for the given clazz, name pair to both the constructor and
     * the representer.
     */
    private static void addAlias(Class<?> clazz, String name, Constructor constructor, Representer representer) {
        representer.addClassTag(clazz, new Tag("!" + name));
        constructor.addTypeDescription(new TypeDescription(clazz, "!" + name));
    }

    /**
     * Read filename and parse it (with the object returned by getYamlObject())
     * into a CuratorConfig.
     */
    public static CuratorConfig loadYamlConfig(String filename) throws FileNotFoundException {
        return getYamlObject().loadAs(new FileReader(filename), CuratorConfig.class);
    }

    /**
     * Convert the given CuratorConfig into a YAML file (using the object
     * returned by getYamlObject()).
     */
    public static String writeYamlConfig(CuratorConfig config) {
        return getYamlObject().dumpAsMap(config);
    }

}
