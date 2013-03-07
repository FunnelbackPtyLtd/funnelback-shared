package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;
import com.funnelback.publicui.search.web.views.freemarker.FallbackFreeMarkerViewResolver;

import freemarker.template.Template;

/**
 * Controller for the results shopping cart.
 * 
 * @since 12.4
 *
 */
@Controller
@SessionAttributes(SessionInterceptor.SEARCH_USER_ATTRIBUTE)
public class ResultsCartController extends SessionControllerBase {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private ResultsCartRepository cartRepository;

    @Autowired
    private FallbackFreeMarkerViewResolver viewResolver;
    
    @Autowired
    private FreeMarkerConfig freemarkerConfig;
    
    @Autowired
    private LocaleResolver localeResolver;
    
    @Autowired
    private MailSender mailSender;

    @RequestMapping(value="/cart-list.json")
    public void cartList(
        @RequestParam("collection") String collectionId,
        @ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
        HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collectionId);
        if (c != null) {
            Map<String, Result> cart = cartRepository.getCart(user, c);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(STATUS, OK);
            map.put("cart", cart);
            map.put("cart-keys", cart.keySet());
            
            sendResponse(response, HttpServletResponse.SC_OK, map);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
        }
    }
    
    /**
     * Adds a single result to the cart. Related collection is taken
     * from the result itself.
     * 
     * @param result
     * @param query
     * @param user
     * @param response
     * @throws IOException
     */
    @RequestMapping(value="/cart-add.json")
    public void cartAdd(
            Result result,
            String query,
            @ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(result.getCollection());
        if (c != null) {
            cartRepository.addToCart(user, c, result, query);
            sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+result.getCollection()+"'"));
        }
    }
    
    /**
     * Remove a single entry from the cart, for the given collection and based
     * on the result URL.
     * 
     * @param collectionId
     * @param url
     * @param user
     * @param response
     * @throws IOException
     */
    @RequestMapping(value="/cart-remove.json")
    public void cartRemove(
            @RequestParam("collection") String collectionId,
            String url,
            @ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collectionId);
        if (c != null) {
            cartRepository.removeFromCart(user, c, url);
            sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
        }
    }
    
    /**
     * Completely clear the result cart for the given collection
     * @param collectionId
     * @param user
     * @param response
     * @throws IOException
     */
    @RequestMapping(value="/cart-clear.json")
    public void cartClear(
            @RequestParam("collection") String collectionId,
            @ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
            HttpServletResponse response) throws IOException {
        
        Collection c = configRepository.getCollection(collectionId);
        if (c != null) {
            cartRepository.clearCart(user, c);
            sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
        }
    }
    
    @RequestMapping(value="/cart-email.json")
    public void cartSendEmail(
            @RequestParam("collection") String collectionId,
            @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile, 
            @ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
            @RequestParam(value="email", required=false) String email,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Collection collection = configRepository.getCollection(collectionId);
        if (collection != null) {
            // Update user email if a new email was passed in
            if (email != null) {
                user.setEmail(email);
            }
            
            Map<String, Result> cart = cartRepository.getCart(user, collection);
            
            if (cart.isEmpty()) {
                sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Cart is empty"));
            } else if (user.getEmail() == null) {
                sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("No email address"));
            } else {
                
                Map<String, Object> model = new HashMap<>();
                model.put("cart", cart);
                model.put("searchUrl", getSearchUrl(request, collection));
    
                String viewName = DefaultValues.FOLDER_CONF + "/"
                        + collection.getId()    + "/"
                        + profile + "/"
                        + DefaultValues.CART_EMAIL_FORM;
    
                FreeMarkerView view = (FreeMarkerView) viewResolver.resolveViewName(viewName, localeResolver.resolveLocale(request));
                if (! view.getUrl().contains(DefaultValues.CART_EMAIL_FORM)) {
                    // View fell back on default "template not found" view which is not
                    // suitable
                    throw new IllegalStateException("Template "+viewName+" or parent not found");
                }
                
                Template tpl = freemarkerConfig.getConfiguration().getTemplate(view.getUrl(), localeResolver.resolveLocale(request));
                
                String content = FreeMarkerTemplateUtils.processTemplateIntoString(tpl, model);
                
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(collection.getConfiguration().value(
                        Keys.ModernUI.Session.CART_EMAIL_FROM,
                        DefaultValues.ModernUI.Session.CART_EMAIL_FROM));
                msg.setSubject(collection.getConfiguration().value(
                        Keys.ModernUI.Session.CART_EMAIL_SUBJECT,
                        DefaultValues.ModernUI.Session.CART_EMAIL_SUBJECT));
                msg.setText(content);
                msg.setTo(user.getEmail());
                
                mailSender.send(msg);
                
                sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
            }
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
        }
    }

    @SneakyThrows(MalformedURLException.class)
    private String getSearchUrl(HttpServletRequest request, Collection c) {
        URL u = new URL(request.getRequestURL().toString());
        
        return u.getProtocol() + "://"
                + u.getHost()
                + ((u.getPort() > 0) ? ":"+u.getPort() : "")
                + u.getPath().substring(0, u.getPath().lastIndexOf('/')+1)
                + c.getConfiguration().value(Keys.ModernUI.SEARCH_LINK, DefaultValues.ModernUI.SEARCH_LINK)
                + "?" + RequestParameters.COLLECTION + "=" + c.getId();        
    }
    
    
}
