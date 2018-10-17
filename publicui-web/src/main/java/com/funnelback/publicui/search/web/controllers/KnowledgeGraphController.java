package com.funnelback.publicui.search.web.controllers;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.profile.ProfileId;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * TODO
 */
@Controller
public class KnowledgeGraphController {

    @RequestMapping("/knowledge-graph.html")
    public void returnStubKnowledgeGraphHtml(
            String collection, // TODO - Use a better type that validates
            String profile, // TODO - Use a better type that validates
            String liveUrl,
            HttpServletResponse response) throws IOException {

        // TODO - We should probably use Spring's ViewResolver instead of dumping the HTML directly in here
        response.setContentType("text/html");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("<!DOCTYPE html>\n"
                + "<html lang=\"en-us\">\n"
                + "<head>\n"
                + "  <meta charset=\"utf-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <meta name=\"robots\" content=\"nofollow\">\n"
                + "  <!--[if IE]><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><![endif]-->\n"
                + "  <title>Funnelback Knowledge Graph</title>\n"
                + "  <link rel=\"stylesheet\" href=\"/s/resources-global/css/font-awesome-4.7.0.min.css\" />\n"
                + "  <link rel=\"stylesheet\" href=\"/s/resources-global/css/nprogress-2.0.0.min.css\" />\n"
                + "  <link rel=\"stylesheet\" href=\"/s/resources-global/css/funnleback.knowledge-graph-2.7.0.min .css\" />\n"
                + "  <!--[if lt IE 9]>\n"
                + "    <script src=\"/s/resources-global/thirdparty/html5shiv.js\"></script>\n"
                + "    <script src=\"/s/resources-global/thirdparty/respond.min.js\"></script>\n"
                + "  <![endif]-->\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class=\"loader\"><span class=\"fa fa-fw fa-3x fa-spinner fa-pulse\"></span><span class=\"text\">Loading...</span></div>\n"
                + "<script type=\"text/javascript\" src=\"/s/resources-global/js/jquery/jquery-1.10.2.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"/s/resources-global/js/nprogress-0.2.0.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"/s/resources-global/js/moment-2.19.2.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"/s/resources-global/js/handlebars-4.0.10.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"/s/resources-global/js/funnleback.knowledge-graph-2.7.0.min.js\"></script>\n"
                + "<script type=\"text/javascript\">\n"
                + "  jQuery(document).ready(function() {\n"
                + "    jQuery(document).knowledgeGraph({\n"
                + "      apiBase: window.location.origin,\n"
                + "      contentFetcher: 'share',\n"
                + "      trigger: 'full',\n"
                + "      templatesFile: '/s/resources/"
                + "          " + UrlEscapers.urlPathSegmentEscaper().escape(collection) + "/"
                + "          " + UrlEscapers.urlPathSegmentEscaper().escape(profile) + "/"
                + "      searchUrl: window.location.origin + '/s/search.json',\n"
                + "      searchParams: {\n"
                + "        collection: '" + StringEscapeUtils.escapeEcmaScript(collection) + "',\n"
                + "        profile: '" + StringEscapeUtils.escapeEcmaScript(profile) + "',\n"
                + "      },\n"
                + "    });\n"
                + "  });\n"
                + "</script>\n"
                + "</body>\n"
                + "</html>");
        }
    }

}
