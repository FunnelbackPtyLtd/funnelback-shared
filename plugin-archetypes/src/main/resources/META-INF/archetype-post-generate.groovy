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
projectPath = Paths.get(request.outputDirectory, request.artifactId)

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
boolean isFilteringEnabled = Boolean.parseBoolean(properties.get("filtering"))

// Remove non alpha numeric chars
pluginPrefix = request.artifactId.replaceAll("[^a-zA-Z0-9]"," ")

// make the first letter of each word capitalized for the class name
pluginClassPrefix = WordUtils.capitalizeFully(pluginPrefix).replaceAll(" ", "")

tmp = projectPath.resolve(request.version + "/tmp")
resources = projectPath.resolve(request.version + "/src/main/resources")
propertiesFile = resources.resolve("funnelback-plugin-" + request.artifactId + ".properties").toFile()

if(isGathererEnabled) {
    String gathererImplementation = "CustomGatherPlugin"
    String gathererInterface = "com.funnelback.plugin.gatherer.PluginGatherer"
    enableImplementation(gathererImplementation)
    writeToPropertiesFile(gathererImplementation, gathererInterface)
}

if(isIndexingEnabled) {
    String indexingImplementation = "IndexingPlugin"
    String indexingInterface = "com.funnelback.plugin.index.IndexingConfigProvider"
    enableImplementation(indexingImplementation)
    writeToPropertiesFile(indexingImplementation, indexingInterface)
}

if(isFacetsEnabled) {
    String facetsImplementation = "FacetsPlugin"
    String facetsInterface = "com.funnelback.plugin.facets.FacetProvider"
    enableSourceImplementation(facetsImplementation)
    enableTestImplementation(facetsImplementation)
    writeToPropertiesFile(facetsImplementation, facetsInterface)
}

if(isSearchLifeCycleEnabled) {
    String searchLifeCycleImplementation = "SearchLifeCycle"
    String searchLifeCycleInterface = "com.funnelback.plugin.SearchLifeCyclePlugin"
    enableImplementation(searchLifeCycleImplementation)
    writeToPropertiesFile(searchLifeCycleImplementation, searchLifeCycleInterface)
}

if(isFilteringEnabled) {
    String filteringImplementation = "CustomFilter"
    enableImplementation(filteringImplementation)
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

def enableImplementation(String impl) {
    srcTarget = projectPath.resolve(request.version + "/src/main/java/" + packagePath)
    testTarget = projectPath.resolve(request.version + "/src/test/java/" + packagePath)

    enableImplementation(impl, impl + ".java", srcTarget)
    enableImplementation(impl, impl + "Test.java", testTarget)
}

def enableSourceImplementation(String impl) {
    srcTarget = projectPath.resolve(request.version + "/src/main/java/" + packagePath)
    enableImplementation(impl, impl + ".java", srcTarget)
}

def enableTestImplementation(String impl) {
    testTarget = projectPath.resolve(request.version + "/src/test/java/" + packagePath)
    enableImplementation(impl, impl + "Test.java", testTarget)
}

def enableImplementation(String impl, String className, Path target) {
    Path source = tmp.resolve(className)
    Path destination = target.resolve(pluginClassPrefix + className)
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
    def fileCreated = destination.toFile()
    // Change the internal reference of class name inside file
    def newContent= fileCreated.text.replace("class " + impl, "class " + pluginClassPrefix + impl)
    fileCreated.text = newContent
}

// Write entry to funnelback-plugin properties file
def writeToPropertiesFile(String impl, String qualifiedInterface) {
    propertiesFile.append(qualifiedInterface + "=" + packageName + "." + pluginClassPrefix + impl + "\n")
}