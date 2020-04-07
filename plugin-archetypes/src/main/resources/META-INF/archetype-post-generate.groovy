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
boolean isJsoupFilteringEnabled = Boolean.parseBoolean(properties.get("jsoup-filtering"))


// Remove non alpha numeric chars
pluginPrefix = request.artifactId.replaceAll("[^a-zA-Z0-9]"," ")

// make the first letter of each word capitalized for the class name
pluginClassPrefix = WordUtils.capitalizeFully(pluginPrefix).replaceAll(" ", "")

tmp = projectPath.resolve("tmp")
resources = projectPath.resolve("src/main/resources")
propertiesFile = resources.resolve("funnelback-plugin-" + request.artifactId + ".properties").toFile()

if(isGathererEnabled) {
    String pluginImplementation = "PluginGatherer"
    String pluginInterface = "com.funnelback.plugin.gatherer.PluginGatherer"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isIndexingEnabled) {
    String pluginImplementation = "IndexingConfigProvider"
    String pluginInterface = "com.funnelback.plugin.index.IndexingConfigProvider"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isFacetsEnabled) {
    String pluginImplementation = "FacetProvider"
    String pluginInterface = "com.funnelback.plugin.facets.FacetProvider"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isSearchLifeCycleEnabled) {
    String pluginImplementation = "SearchLifeCyclePlugin"
    String pluginInterface = "com.funnelback.plugin.SearchLifeCyclePlugin"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isFilteringEnabled) {
    String pluginImplementation = "StringFilter"
    enableImplementationAndTests(pluginImplementation)
}

if(isJsoupFilteringEnabled) {
    String pluginImplementation = "JsoupFilter"
    enableImplementationAndTests(pluginImplementation)
}

writePluginPropsFileTest();

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

def enableImplementationAndTests(String impl) {
    enableSourceImplementation(impl)
    enableTests(impl)
}

def enableSourceImplementation(String impl) {
    srcTarget = projectPath.resolve("src/main/java/" + packagePath)
    prepareSourceFiles(impl, impl + ".java", srcTarget)
}

def enableTests(String impl) {
    testTarget = projectPath.resolve("src/test/java/" + packagePath)
    prepareSourceFiles(impl, impl + "Test.java", testTarget)
}

def prepareSourceFiles(String impl, String className, Path target) {
    Path source = tmp.resolve(className)
    Path destination = target.resolve(pluginClassPrefix + className)
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
    def fileCreated = destination.toFile()
    // Change the internal reference of class name inside file
    def newContent= fileCreated.text
       //.replace("class " + impl, "class " + pluginClassPrefix + impl)
      .replace("_ClassNamePrefix_", pluginClassPrefix);
    
    fileCreated.text = newContent
}

// Write entry to funnelback-plugin properties file
def writeToPropertiesFile(String impl, String qualifiedInterface) {
    propertiesFile.append(qualifiedInterface + "=" + packageName + "." + pluginClassPrefix + impl + "\n")
}

def writePluginPropsFileTest() {
    Path source = tmp.resolve("PluginPropsFileTest.java");
    Path destination = testTarget.resolve("PluginPropsFileTest.java");
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
}
