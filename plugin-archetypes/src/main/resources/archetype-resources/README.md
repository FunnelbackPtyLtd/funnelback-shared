## Setting up PluginUtils

In order to setup your plugin to work as expected PluginUtils.java needs to be configured with correct values.
This file resides under `src/main/java/{packageName}/PluginUtils.java`.

This file uses below fields:
* PluginConfigKey
* PluginConfigKeyEncrypted
* ConfigFiles
* Audience
* MarketPlaceSubType
* ProductTopic
* ProductSubtopic
* PluginId
* PluginName
* PluginDescription
* PluginTargets
* FilterClasses
* JsoupFilterClasses

## Setting up documentation

Access `src/main/resources/ascii/sections` folder for documentation sections.

The below files need to have accurate information based upon your plugin details:
1. `additionalconfigsettings.adoc`: This file contains additional information about config settings to be done in order to execute plugin successfully.
2. `changelog.adoc`: This document should contain all the change logs for all versions of this plugin. An example can be found here: `https://keepachangelog.com/en/1.0.0/`
3. `example.adoc`: This document should have an appropriate example of your plugin functionality.
4. `keywords.adoc`: Enlist all keywords related to your plugin here.

**Note**: Please refer to documentation for syntax help while writing ascii docs https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/

During the Maven install phase a new document will be created which has complete information about your plugin. This new document consolidates information from the PluginUtils class and above documents. The consolidated document can be found under *src/main/resources* as well as in *target/{plugin-version}/docs* folder.

The consolidated document generation happens in the Maven process after the clean install phases when using below command in terminal:
```bash
mvn clean install
```

**Note**: The above command also generates *plugin-schema.json* which contains the structure of your plugin configuration details.

**Note**: *index.adoc* and *plugin-schema.json* files are generated each time you compile the plugin, and hence changes made to these two files take no effect after each build. 