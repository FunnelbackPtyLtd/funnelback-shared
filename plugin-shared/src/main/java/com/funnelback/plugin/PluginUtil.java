package com.funnelback.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;

import com.funnelback.common.utils.SharedXMLUtils;

public class PluginUtil {

    // Semver validation regex, see https://semver.org/ for details
    static final Pattern semverPattern = Pattern.compile(
            // required major.minor.patch
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
                    // optional pre-release string; hyphen followed by dot separated identifiers
                    "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
                    // optional build metadata string; plus sign followed by dot separated identifiers
                    "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    public static boolean matchesSemver(String version) {
        return semverPattern.matcher(version).matches();
    }

    /**
     * Reads the pom.xml file for the current project, and returns the value of the version node.
     *
     * @return the current maven project version
     */
    public static Optional<String> getCurrentProjectVersion() {
        try {
            // Use secure DocumentBuilder from SharedXMLUtils to prevent XXE attacks
            DocumentBuilder builder = SharedXMLUtils.getDocumentBuilder();
            File file = new File("pom.xml");
            String version =
                    builder.parse(file).getDocumentElement().getElementsByTagName("version").item(0).getTextContent();
            return (version != null) ?  Optional.of(version) : Optional.empty();
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
