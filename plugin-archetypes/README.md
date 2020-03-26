
## Example usage of the plugin archetypes

### Plugin Implementations

Currently we support generating multiple implementations of several plugin interfaces. 
You can enable or disable one or more implementations inside the same plugin using following flags.


#### Plugin flags

- `gatherer` 
- `indexing` 
- `facets` 
- `searchLifeCycle`

#### Filtering flags

- `filtering`

### Instructions

- All the implementations and filtering are enabled by default. 
- You may need to disable the implementations you do not intend to use.
- If you disable them manually by deleting the implementation classes, 
make sure you delete the appropriate entries in the `funnelback-plugin-{plugin-name}.properties` file as well.
(this applies only for the plugin implementations not for the filtering).

### Examples

Ex: Enable indexing and gatherer inside a plugin

```
mvn archetype:generate -DarchetypeGroupId=com.funnelback    \
    -DarchetypeArtifactId=plugin-archetypes                 \
    -DarchetypeVersion=15.25.2011-SNAPSHOT                  \
    -DgroupId=com.example                                   \
    -DartifactId=example-plugin                             \
    -Dversion=1.0                                           \
    -Dgatherer=true                                         \
    -Dindexing=true                                         \
    -Dfacets=false                                          \
    -DsearchLifeCycle=false                                 \
    -Dfiltering=false
```

Ex: Enable all the implementations inside a plugin

```
mvn archetype:generate -DarchetypeGroupId=com.funnelback    \
    -DarchetypeArtifactId=plugin-archetypes                 \
    -DarchetypeVersion=15.25.2011-SNAPSHOT                  \
    -DgroupId=com.example                                   \
    -DartifactId=example-plugin                             \
    -Dversion=1.0                                           
```