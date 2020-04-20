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
    String pluginImplementation = "_ClassNamePrefix_PluginGatherer"
    String pluginInterface = "com.funnelback.plugin.gatherer.PluginGatherer"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isIndexingEnabled) {
    String pluginImplementation = "_ClassNamePrefix_IndexingConfigProvider"
    String pluginInterface = "com.funnelback.plugin.index.IndexingConfigProvider"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isFacetsEnabled) {
    String pluginImplementation = "_ClassNamePrefix_FacetProvider"
    String pluginInterface = "com.funnelback.plugin.facets.FacetProvider"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isSearchLifeCycleEnabled) {
    String pluginImplementation = "_ClassNamePrefix_SearchLifeCyclePlugin"
    String pluginInterface = "com.funnelback.plugin.SearchLifeCyclePlugin"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if(isFilteringEnabled) {
    String pluginImplementation = "_ClassNamePrefix_StringFilter"
    enableImplementationAndTests(pluginImplementation)
}

if(isJsoupFilteringEnabled) {
    String pluginImplementation = "_ClassNamePrefix_JsoupFilter"
    enableImplementationAndTests(pluginImplementation)
}

writePluginPropsFileTest();

enableSourceImplementation("_ClassNamePrefix_PluginUtils");

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

def enableImplementationAndTests(String originalClassName) {
    enableSourceImplementation(originalClassName)
    enableTests(originalClassName)
}

def enableSourceImplementation(String originalClassName) {
    srcTarget = projectPath.resolve("src/main/java/" + packagePath)
    prepareSourceFiles(originalClassName + ".java", srcTarget)
}

def enableTests(String originalClassName) {
    testTarget = projectPath.resolve("src/test/java/" + packagePath)
    prepareSourceFiles(originalClassName + "Test.java", testTarget)
}

def prepareSourceFiles(String originalClassName, Path target) {
    Path source = tmp.resolve(originalClassName)
    Path destination = target.resolve(originalClassName.replace("_ClassNamePrefix_", pluginClassPrefix));
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
    def fileCreated = destination.toFile()
    // Change the internal reference of class name inside file
    def newContent= fileCreated.text
      .replace("_ClassNamePrefix_", pluginClassPrefix);
    
    fileCreated.text = newContent
}

// Write entry to funnelback-plugin properties file
def writeToPropertiesFile(String originalClassName, String qualifiedInterface) {
    propertiesFile.append(qualifiedInterface + "=" + packageName + "." + originalClassName.replace("_ClassNamePrefix_", pluginClassPrefix) + "\n")
}

def writePluginPropsFileTest() {
    enableTests("PluginPropsFile");
    //Path source = tmp.resolve("PluginPropsFileTest.java");
    //Path destination = testTarget.resolve("PluginPropsFileTest.java");
    //Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
}
