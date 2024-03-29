import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
@Grab(group='org.apache.commons', module='commons-lang3', version='3.12.0')
import org.apache.commons.lang3.StringUtils

// the path where the project got generated
projectPath = Paths.get(request.outputDirectory, request.artifactId)

// the properties available to the archetype
properties = request.properties

// The plugin package name
packageName = request.package

// Clean up the existing packageName which is probably wrong, I don't know what males it.
// this is because we can not clean up the artifact ID in archetype-metadata.xml
deletePackageFolders(packageName)

// Create directory structure
toMainSrcPath(packageName).toFile().mkdirs()
toTestSrcPath(packageName).toFile().mkdirs()

boolean isGathererEnabled = Boolean.parseBoolean(properties.get("gatherer"))
boolean isIndexingEnabled = Boolean.parseBoolean(properties.get("indexing"))
boolean isFacetsEnabled = Boolean.parseBoolean(properties.get("facets"))
boolean isSearchLifeCycleEnabled = Boolean.parseBoolean(properties.get("searchLifeCycle"))
boolean isFilteringEnabled = Boolean.parseBoolean(properties.get("filtering"))
boolean isJsoupFilteringEnabled = Boolean.parseBoolean(properties.get("jsoup-filtering"))
boolean isServletFilteringEnabled = Boolean.parseBoolean(properties.get("search-servlet-filtering"))
boolean isStartUrlProviderEnabled = Boolean.parseBoolean(properties.get("start-url-provider"))

// Remove non alpha numeric chars
pluginPrefix = request.artifactId.replaceAll("[^a-zA-Z0-9]"," ")

// make the first letter of each word capitalized for the class name
pluginClassPrefix = StringUtils.capitalize(pluginPrefix).replaceAll(" ", "")

tmp = projectPath.resolve("tmp")
resources = projectPath.resolve("src/main/resources")
propertiesFile = resources.resolve("funnelback-plugin-" + request.artifactId + ".properties").toFile()
pluginUtilsFilterClass = null
pluginUtilsJsoupFilterClass = null

if (isGathererEnabled) {
    String pluginImplementation = "_ClassNamePrefix_PluginGatherer"
    String pluginInterface = "com.funnelback.plugin.gatherer.PluginGatherer"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if (isIndexingEnabled) {
    String pluginImplementation = "_ClassNamePrefix_IndexingConfigProvider"
    String pluginInterface = "com.funnelback.plugin.index.IndexingConfigProvider"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if (isFacetsEnabled) {
    String pluginImplementation = "_ClassNamePrefix_FacetProvider"
    String pluginInterface = "com.funnelback.plugin.facets.FacetProvider"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if (isSearchLifeCycleEnabled) {
    String pluginImplementation = "_ClassNamePrefix_SearchLifeCyclePlugin"
    String pluginInterface = "com.funnelback.plugin.SearchLifeCyclePlugin"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if (isFilteringEnabled) {
    String pluginImplementation = "_ClassNamePrefix_StringFilter"
    enableImplementationAndTests(pluginImplementation)
    pluginUtilsFilterClass = pluginImplementation
}

if (isJsoupFilteringEnabled) {
    String pluginImplementation = "_ClassNamePrefix_JsoupFilter"
    enableImplementationAndTests(pluginImplementation)
    pluginUtilsJsoupFilterClass = pluginImplementation
}

if (isServletFilteringEnabled) {
    String pluginImplementation = "_ClassNamePrefix_SearchServletFilterPlugin"
    String pluginInterface = "com.funnelback.plugin.servlet.filter.SearchServletFilterHook"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

if (isStartUrlProviderEnabled) {
    String pluginImplementation = "_ClassNamePrefix_StartUrlProviderPlugin"
    String pluginInterface = "com.funnelback.plugin.starturl.StartUrlProvider"
    enableImplementationAndTests(pluginImplementation)
    writeToPropertiesFile(pluginImplementation, pluginInterface)
}

writePluginPropsFileTest()
writePluginUtilsTest()

enableSourceImplementation("PluginUtils")

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

// Replace contents of all files
Files.walkFileTree(projectPath.resolve("src"), new SimpleFileVisitor<Path>() {

    //delete all files inside tmp
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE
    }

    // delete directory
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE
    }
})

def getPluginClassName(String originalClassName) {
    return originalClassName.replace("_ClassNamePrefix_", pluginClassPrefix)
}

def getPluginFullyQualifiedClassName(String originalClassName) {
    return packageName + "." + getPluginClassName(originalClassName)
}

def enableImplementationAndTests(String originalClassName) {
    enableSourceImplementation(originalClassName)
    enableTests(originalClassName)
}

def enableSourceImplementation(String originalClassName) {
    srcTarget = toMainSrcPath(packageName)
    prepareSourceFiles(originalClassName + ".java", srcTarget)
}

def enableTests(String originalClassName) {
    testTarget = toTestSrcPath(packageName)
    prepareSourceFiles(originalClassName + "Test.java", testTarget)
}

def prepareSourceFiles(String originalClassName, Path target) {
    Path source = tmp.resolve(originalClassName)
    Path destination = target.resolve(getPluginClassName(originalClassName))
    println "Copy " + source + " to " + destination
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
    def fileCreated = destination.toFile()

    // Change the internal reference of class name inside file
    def newContent= fileCreated.text.replace("_ClassNamePrefix_", pluginClassPrefix)

    if (originalClassName == "PluginUtils.java") {
        newContent = replaceTargetType(newContent)
        newContent = replaceFilterClasses(newContent)
    }
    fileCreated.text = newContent
}

// Replace plugin targets (scope) in PluginUtils.class
def replaceTargetType(String text) {
    def result = new ArrayList<String>()

    if (Boolean.parseBoolean(properties.get("runs-on-datasource"))) {
        result.add("PluginTarget.DATA_SOURCE")
    }
    if (Boolean.parseBoolean(properties.get("runs-on-result-page"))) {
        result.add("PluginTarget.RESULTS_PAGE")
    }

    return text.replace("__plugin_target__", StringUtils.join(result, ", "))
}

// Replace filter classes entry in PluginUtils.class
def replaceFilterClasses(String text) {
    def filterClass =  {s -> s ? '"' + getPluginFullyQualifiedClassName(s) + '"' : 'null'}
    return text
            .replace("__plugin_filterClass__", filterClass(pluginUtilsFilterClass))
            .replace("__plugin_jsoupFilterClass__", filterClass(pluginUtilsJsoupFilterClass))
}

// Write entry to funnelback-plugin properties file
def writeToPropertiesFile(String originalClassName, String qualifiedInterface) {
    propertiesFile.append(qualifiedInterface + "=" + getPluginFullyQualifiedClassName(originalClassName) + "\n")
}

def writePluginPropsFileTest() {
    enableTests("PluginPropsFile")
}

def writePluginUtilsTest() {
    enableTests("PluginUtils")
}

def toMainSrcPath(String packageName) {
    return projectPath.resolve("src/main/java/" + packageName.replace(".", "/"))
}

def toTestSrcPath(String packageName) {
    return projectPath.resolve("src/test/java/" + packageName.replace(".", "/"))
}

def deletePackageFolders(String packageName) {
    deleteEmptyDir(toMainSrcPath(packageName))
    deleteEmptyDir(toTestSrcPath(packageName))
}

def deleteEmptyDir(Path dir) {
    println "deleting: " + dir
    Files.delete(dir)
    if (Files.exists(dir)) {
        throw new RuntimeException("Expected to be able to delete: " + dir)
    }
}
