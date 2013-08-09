package com.funnelback.publicui.search.web.controllers.session;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

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

@Controller
@Log4j
public class ResultCartProcessController extends SessionController {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ResultsCartRepository cartRepository;

    /**
     * Processes the cart
     * @param collection Collection to process the cart for
     * @param profile Profile to process the cart for
     * @param user Current user
     * @param response {@link HttpServletResponse}
     * @return 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @RequestMapping(value="/cart-process.html")
    public ModelAndView cartProcess(
        @RequestParam(required=true) String collection,
        @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile,
        @ModelAttribute SearchUser user,
        HttpServletResponse response) throws InstantiationException, IllegalAccessException {
        
        Collection c = configRepository.getCollection(collection);
        if (c != null) {
            Class<?> clazz = c.getCartProcessClass();
            if (clazz == null) {
                // Default empty implementation
                clazz = CartProcessController.class;
            }
            
            CartProcessController ctrl = (CartProcessController) clazz.newInstance();
            List<CartResult> cart = cartRepository.getCart(user, c);
            ModelAndView mav = ctrl.process(c, user, cart);
            
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
            // FIXME: Error page? Collection list?
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        return null;
        
    }

}
