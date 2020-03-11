import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
@Grab(group='org.apache.commons', module='commons-text', version='1.8')
import org.apache.commons.text.WordUtils

// the path where the project got generated
Path projectPath = Paths.get(request.outputDirectory, request.artifactId)

// the properties available to the archetype
properties = request.properties

// the Java package of the generated project, e.g. com.acme
packageName = request.packageName

// convert it into a path, e.g. com/acme
packagePath = packageName.replace(".", "/")

boolean isGathererEnabled = Boolean.parseBoolean(properties.get("gatherer"))
boolean isIndexingEnabled = Boolean.parseBoolean(properties.get("indexing"))
boolean isFacetsEnabled = Boolean.parseBoolean(properties.get("facets"))
boolean isSearchLifeCycleEnabled = Boolean.parseBoolean(properties.get("searchLifeCycle"))

// Remove non alpha numeric chars
pluginPrefix = request.artifactId.replaceAll("[^a-zA-Z0-9]"," ")

// make the first letter of each word capitalized for the class name
pluginClassPrefix = WordUtils.capitalizeFully(pluginPrefix).replaceAll(" ", "")

target = projectPath.resolve(request.version + "/src/main/java/" + packagePath)
tmp = projectPath.resolve(request.version + "/tmp")
resources = projectPath.resolve(request.version + "/src/main/resources")
propertiesFile = resources.resolve("funnelback-plugin-" + request.artifactId + ".properties").toFile()

if(isGathererEnabled) {
    enableImplementation("CustomGatherPlugin", "com.funnelback.plugin.gatherer.PluginGatherer")
}

if(isIndexingEnabled) {
    enableImplementation("IndexingPlugin", "com.funnelback.plugin.index.IndexingConfigProvider")
}

if(isFacetsEnabled) {
    enableImplementation("FacetsPlugin", "com.funnelback.plugin.facets.FacetProvider")
}

if(isSearchLifeCycleEnabled) {
    enableImplementation("SearchLifeCycle", "com.funnelback.plugin.SearchLifeCyclePlugin")
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

def enableImplementation(String impl, String qualifiedInterface) {
    String className = impl + ".java"
    Path source = tmp.resolve(className)
    Path destination = target.resolve(pluginClassPrefix + className)
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
    def fileCreated = destination.toFile()
    // Change the internal reference of class name
    def newContent= fileCreated.text.replace("class " + impl, "class " + pluginClassPrefix + impl)
    fileCreated.text = newContent
    propertiesFile.append(qualifiedInterface + "=" + packageName + "." + pluginClassPrefix + impl + "\n")
}