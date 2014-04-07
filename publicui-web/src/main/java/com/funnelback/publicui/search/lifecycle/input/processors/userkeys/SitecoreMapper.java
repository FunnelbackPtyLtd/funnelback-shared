package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
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

        //Assemble the url to query
        String serviceUrl =

            //Pull the start of the URL from collection.cfg
            //e.g. http://asc/sitecore/shell/userservice/userservice.asmx/UserGroups?username=
            currentCollection.getConfiguration().value(Keys.SecurityEarlyBinding.SITECORE_SERVICE_URL)

            //Attach the current search user to the end
            + transaction.getQuestion().getPrincipal().getName();

        System.out.println(currentCollection.getConfiguration().value(Keys.SecurityEarlyBinding.SITECORE_SERVICE_URL));
        System.out.println(transaction.getQuestion().getPrincipal().getName());

        //Storage for keys to return
        List<String> userKeys = new ArrayList<String>();

        try {
            //Parse the xml
            Document doc =
                DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(serviceUrl);

            doc.normalize();

            String payload = doc
                .getElementsByTagName(xmlTagOfInterest)
                .item(0)
                .getTextContent();

            //Split the comma-separated values and add then to the return list
            for (String s : payload.split(",")) {
                userKeys.add(s.trim());
            }
        } catch (Exception e) {
            log.error("Hit " + e.toString() + " trying to parse xml from " + serviceUrl, e);
        }
        return userKeys;
    }
}
