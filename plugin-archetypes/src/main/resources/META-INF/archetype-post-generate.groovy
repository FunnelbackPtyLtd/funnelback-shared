import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes;

// the path where the project got generated
Path projectPath = Paths.get(request.outputDirectory, request.artifactId)

// the properties available to the archetype
Properties properties = request.properties

// the Java package of the generated project, e.g. com.acme
String groupId = request.groupId

// convert it into a path, e.g. com/acme
String packagePath = groupId.replace(".", "/")

boolean isGathererEnabled = Boolean.parseBoolean(properties.get("gatherer"))
boolean isIndexingEnabled = Boolean.parseBoolean(properties.get("indexing"))
boolean isFacetsEnabled = Boolean.parseBoolean(properties.get("facets"))
boolean isSearchLifeCycleEnabled = Boolean.parseBoolean(properties.get("searchLifeCycle"))

// Remove non alpha numeric chars
String pluginPrefix = request.artifactId.replaceAll("[^a-zA-Z0-9]","")

// make the first letter capitalized for the class name
String pluginClassPrefix = pluginPrefix.substring(0, 1).toUpperCase() + pluginPrefix.substring(1)

Path target = projectPath.resolve(request.version + "/src/main/java/" + packagePath)
Path tmp = projectPath.resolve(request.version + "/tmp")
Path resources = projectPath.resolve(request.version + "/src/main/resources")
File propertiesFile = resources.resolve("funnelback-plugin-" + request.artifactId + ".properties").toFile()

if(isGathererEnabled) {
    String gathererPlugin = "CustomGatherPlugin.java";
    Path gatherer = tmp.resolve(gathererPlugin)
    Files.copy(gatherer, target.resolve(pluginClassPrefix + gathererPlugin), StandardCopyOption.REPLACE_EXISTING)
    propertiesFile.append("com.funnelback.plugin.gatherer.GathererPlugin=" + groupId + "." + pluginClassPrefix + "CustomGatherPlugin\n")
}

if(isIndexingEnabled) {
    String indexingPlugin = "IndexingPlugin.java";
    Path gatherer = tmp.resolve(indexingPlugin)
    Files.copy(gatherer, target.resolve(pluginClassPrefix + indexingPlugin), StandardCopyOption.REPLACE_EXISTING)
    propertiesFile.append("com.funnelback.plugin.index.IndexingConfigProvider=" + groupId + "." + pluginClassPrefix + "IndexingPlugin\n")
}

if(isFacetsEnabled) {
    String facetsPlugin = "FacetsPlugin.java";
    Path gatherer = tmp.resolve(facetsPlugin)
    Files.copy(gatherer, target.resolve(pluginClassPrefix + facetsPlugin), StandardCopyOption.REPLACE_EXISTING)
    propertiesFile.append("com.funnelback.plugin.facets.FacetProvider=" + groupId + "." + pluginClassPrefix + "FacetsPlugin\n")
}

if(isSearchLifeCycleEnabled) {
    String searchLifeCyclePlugin = "SearchLifeCyclePlugin.java";
    Path gatherer = tmp.resolve(searchLifeCyclePlugin)
    Files.copy(gatherer, target.resolve(pluginClassPrefix + searchLifeCyclePlugin), StandardCopyOption.REPLACE_EXISTING)
    propertiesFile.append("com.funnelback.plugin.SearchLifeCyclePlugin=" + groupId + "." + pluginClassPrefix + "SearchLifeCyclePlugin\n")
}

// Delete tmp directory and files
Files.walkFileTree(tmp, new SimpleFileVisitor<Path>() {

    //delete all files inside tmp
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file)
        return FileVisitResult.CONTINUE
    }

    // delete directory
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir)
        return FileVisitResult.CONTINUE
    }
})
