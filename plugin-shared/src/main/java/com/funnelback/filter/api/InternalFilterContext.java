package com.funnelback.filter.api;

import java.io.File;
import java.util.Optional;

/**
 * An immutable context supplied to the product filters and contains tools to read collection configuration files
 *
 * This interface is only used for product build-in filters
 * Dev should only implement methods from {@link FilterContext}
 * And must not implement methods with the same name as methods declared in this interface
 */
public interface InternalFilterContext {

    /**
     * <p>Provides access to configuration files inside the collection configuration folder.</p>
     *
     * <p>This is useful to access custom collection-level configuration files</p>
     *
     * @param filename Name of the file to access
     * @return A {@link File} path pointing to the desired file inside the collection configuration folder
     */
    public File getCollectionConfigFile(String filename);

    /**
     * Reads a configuration file for the currently running filter.
     *
     * The configuration file read is for the currently running filter for current the data source or search package.
     *
     * @param filename the name of the configuration file to read.
     * @return empty if the file doesn't exist, otherwise the contents of the file.
     */
    public Optional <byte[]> filterConfigurationFileAsBytes(String filename);

    /**
     * Reads a configuration file for the currently running filter as a UTF-8 String.
     *
     * See: {@link #filterConfigurationFile(String)}
     *
     * @param filename the name of the configuration file to read.
     * @return empty if the file doesn't exist, otherwise the contents of the file.
     */
    public Optional<String> filterConfigurationFile(String filename);

}

