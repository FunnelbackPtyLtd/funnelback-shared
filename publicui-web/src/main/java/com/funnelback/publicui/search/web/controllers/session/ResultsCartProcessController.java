package com.funnelback.publicui.search.web.controllers.session;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;

/**
 * <p>Controller to process a result cart.</p>
 * 
 * <p>Processing the cart can be customised by implementing {@link CustomCartProcessor},
 * and the resulting view can use a FreeMarker template defined in <tt>collection.cfg</tt></p>
 * 
 * @since v13.0
 */
@Controller
public class ResultsCartProcessController extends SessionController {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ResultsCartRepository cartRepository;

    /**
     * <p>Processes the cart</p>
     * 
     * <p>Uses the default {@link CustomCartProcessor} or a custom one
     * if configured, then renders a FreeMarker view (Default or configured
     * one)</p>
     * 
     * @param collection Collection to process the cart for
     * @param profile Profile to process the cart for
     * @param user Current user
     * @param request HTTP request
     * @param response HTTP response
     * @return A {@link ModelAndView} to render 
     * @throws InstantiationException If something went wrong initializing the custom processing class
     * @throws IllegalAccessException If something went wrong initializing the custom processing class
     */
    @RequestMapping(value="/cart-process.html")
    public ModelAndView cartProcess(
        @RequestParam(required=true) String collection,
        @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile,
        @ModelAttribute SearchUser user,
        HttpServletRequest request,
        HttpServletResponse response) throws InstantiationException, IllegalAccessException {
        
        Collection c = configRepository.getCollection(collection);
        if (c != null && user != null) {
            Class<?> clazz = c.getCartProcessClass();
            if (clazz == null) {
                // Default empty implementation
                clazz = CustomCartProcessor.class;
            }
            
            CustomCartProcessor ctrl = (CustomCartProcessor) clazz.newInstance();
            List<CartResult> cart = cartRepository.getCart(user, c);
            ModelAndView mav = ctrl.process(c, user, cart, request, response);
            
            mav.addObject(RequestParameters.COLLECTION, c);
            mav.addObject("user", user);
            mav.addObject("cart", cart);
            
            // Patch view name to include full relative path from SEARCH_HOME
            mav.setViewName( DefaultValues.FOLDER_CONF + "/"
                + c.getId()    + "/"
                + profile + "/"
                + mav.getViewName());
            return mav;
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

}
