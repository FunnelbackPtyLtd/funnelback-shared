package com.funnelback.publicui.search.web.controllers.session;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.ProfileId;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.ProfileEditor;

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

    @Autowired
    private LogService logService;

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
        binder.registerCustomEditor(ProfileId.class, new ProfileEditor(DefaultValues.DEFAULT_PROFILE));
    }

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
        @RequestParam(required=true) Collection collection,
        @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) ProfileId profile,
        @ModelAttribute SearchUser user,
        HttpServletRequest request,
        HttpServletResponse response) throws InstantiationException, IllegalAccessException {
        
        if (collection != null && user != null) {
            Class<?> clazz = collection.getCartProcessClass();
            if (clazz == null) {
                // Default empty implementation
                clazz = CustomCartProcessor.class;
            }
            
            CustomCartProcessor ctrl = (CustomCartProcessor) clazz.newInstance();
            List<CartResult> cart = cartRepository.getCart(user, collection);
            
            for(CartResult result: cart) {
                logService.logCart(LogUtils.createCartLog(result.getIndexUrl(), request,
                        collection, CartClickLog.Type.PROCESS_CART, user));
            }
            
            ModelAndView mav = ctrl.process(collection, user, cart, request, response);
            
            mav.addObject(RequestParameters.COLLECTION, collection);
            mav.addObject("user", user);
            mav.addObject("cart", cart);
            
            // Patch view name to include full relative path from SEARCH_HOME
            mav.setViewName( DefaultValues.FOLDER_CONF + "/"
                + collection.getId()    + "/"
                + profile.getId() + "/"
                + mav.getViewName());
            return mav;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

}
