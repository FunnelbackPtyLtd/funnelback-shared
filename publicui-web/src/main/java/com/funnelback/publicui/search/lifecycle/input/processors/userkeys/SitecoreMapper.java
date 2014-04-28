package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import lombok.extern.log4j.Log4j;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Authenticates search users via SiteCore-provided XML.
 */
@Log4j
public class SitecoreMapper implements UserKeysMapper {

    private static final String xmlTagOfInterest = "roles";

    @Override
    public List<String> getUserKeys(Collection currentCollection, SearchTransaction transaction) {

        //Storage for keys to return
        List<String> userKeys = new ArrayList<String>();

        String urlStem = null;
        String searchName = null;
        try {

            //Assemble the url to query
            try {
                //Pull the start of the URL from collection.cfg
                //e.g. http://asc/sitecore/shell/userservice/userservice.asmx/UserGroups?username=
                urlStem = currentCollection.getConfiguration().value(Keys.SecurityEarlyBinding.SITECORE_SERVICE_URL);
                if(urlStem == null) {
                    throw new Exception ("urlStem was null");
                }
            } catch (Exception e) {
                log.error("Unable to get sitecore service url out of collection.cfg", e);
                //Return immediately
                return userKeys;
            }

            
            try {
                //Attach the current search user to the end
                searchName = transaction.getQuestion().getPrincipal().getName();
                if(searchName == null) {
                    throw new Exception("searchName was null");
                }
            } catch (Exception e) {
                log.error("Unable to get principle name from search transaction", e);
                //Return immediately
                return userKeys;
            }

            URL url = new URL(urlStem + searchName);
            InputStream is = url.openStream();

            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            String xmlContents = writer.toString();

            is.close();

            log.debug("XML Contents to follow: ");
            log.debug(xmlContents);

            InputStream xmlIs = new ByteArrayInputStream(xmlContents.getBytes());

            //Parse the xml
            Document doc =
                DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(xmlIs);

            //create a debug log string
            log.debug("LOOKED IN: " + urlStem + searchName);


            String payload = doc
                .getElementsByTagName(xmlTagOfInterest)
                .item(0)
                .getTextContent();

            //Split the comma-separated values and add then to the return list
            for (String s : payload.split(",")) {
                
                //Remove internal spaces - experimental to try to get it working
                userKeys.add(s.replace(" ", "").trim());
            }

            String dbgUserKeys = "FOR USER " + searchName + ", RETURNED USERKEYS:\r\n";
            for(String s : userKeys) {
                dbgUserKeys += s + "\r\n";
            }
            log.debug(dbgUserKeys);

        } catch (Exception e) {
            log.error("Hit " + e.toString() + " trying to parse xml from " + urlStem + " with searchName " + searchName, e);
        }
        return userKeys;
    }
}
