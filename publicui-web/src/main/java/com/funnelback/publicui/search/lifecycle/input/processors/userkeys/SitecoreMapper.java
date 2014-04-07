package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Authenticates search users via SiteCore-provided XML.
 */
public class SitecoreMapper implements UserKeysMapper {

    @Override
    public List<String> getUserKeys(Collection currentCollection, SearchTransaction transaction) {

        //Assemble the url to query
        String serviceUrl =

            //Pull the start of the URL from collection.cfg
            //e.g. http://asc/sitecore/shell/userservice/userservice.asmx/UserGroups?username=
            currentCollection.getConfiguration().value("sitecore_service_url")

            //Attach the current search user to the end
            + transaction.getQuestion().getInputParameterMap().get("REMOTE_USER");

        //Storage for keys to return
        List<String> userKeys = new ArrayList<String>();

        try {
            //Parse the xml
            Element el =
                DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(serviceUrl)
                    .getDocumentElement();
            el.normalize();
            String payload = el.getFirstChild().getTextContent();

            //Split the comma-separated values and add then to the return list
            for (String userKey : payload.split(",")) {
                userKeys.add(userKey.trim());
            }
        } catch (IOException ioe) {

        } catch (ParserConfigurationException pce) {

        } catch (SAXException se) {

        }
        return userKeys;
    }
}
