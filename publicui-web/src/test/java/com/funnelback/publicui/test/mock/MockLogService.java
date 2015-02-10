package com.funnelback.publicui.test.mock;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.apache.commons.lang3.NotImplementedException;

import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.FacetedNavigationLog;
import com.funnelback.publicui.search.model.log.InteractionLog;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;
import com.funnelback.publicui.search.service.log.LogService;

public class MockLogService implements LogService {

    @Getter private List<ClickLog> clickLogs = new ArrayList<ClickLog>();
    @Getter private List<CartClickLog> cartLogs = new ArrayList<CartClickLog>();
    @Getter private List<ContextualNavigationLog> cnLogs = new ArrayList<>();
    @Getter private List<FacetedNavigationLog> fnLogs = new ArrayList<>();
    @Getter private List<PublicUIWarningLog> publicUiWarnings = new ArrayList<>();
    
    @Override
    public void logClick(ClickLog cl) {
        clickLogs.add(cl);
    }

    @Override
    public void logContextualNavigation(ContextualNavigationLog cnl) {
        cnLogs.add(cnl);
    }
    
    @Override
    public void logFacetedNavigation(FacetedNavigationLog fnl) {
        fnLogs.add(fnl);
    }
    
    @Override
    public void logPublicUIWarning(PublicUIWarningLog warning) {
        publicUiWarnings.add(warning);        
    }

	@Override
	public void logInteraction(InteractionLog interactionLog) {
		throw new NotImplementedException("MockLogService doesn't implement logInteraction");
	}

    @Override
    public void logCart(CartClickLog cartLog) {
        cartLogs.add(cartLog);
    }
    
    public void resetCartLog() {
        cartLogs = new ArrayList<CartClickLog>();
    }

}
