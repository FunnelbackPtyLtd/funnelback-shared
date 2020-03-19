
## Example usage of the plugin archetypes


### plugin with multiple types

Currently we support the implementations of several interfaces ie. 

- `gatherer` 
- `indexing` 
- `facets` 
- `searchLifeCycle`
- `filtering`

You can enable one or many of them inside the same plugin using a flag.

Ex: Enable indexing and gatherer in plugin

```
mvn archetype:generate -DarchetypeGroupId=com.funnelback    \
    -DarchetypeArtifactId=plugin-archetypes                 \
    -DarchetypeVersion=15.25.2007-SNAPSHOT                  \
    -DgroupId=com.example                                   \
    -DartifactId=example-plugin                             \
    -Dversion=1.0                                           \
    -Dgatherer=true                                         \
    -Dindexing=true                                         \
    -Dfiltering=true
```
