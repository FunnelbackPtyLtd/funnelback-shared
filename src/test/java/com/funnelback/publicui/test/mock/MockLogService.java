package com.funnelback.publicui.test.mock;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.service.log.LogService;

public class MockLogService implements LogService {

	@Getter private List<ClickLog> clickLogs = new ArrayList<ClickLog>();
	@Getter private List<ContextualNavigationLog> cnLogs = new ArrayList<ContextualNavigationLog>();
	
	@Override
	public void logClick(ClickLog cl) {
		clickLogs.add(cl);
	}

	@Override
	public void logContextualNavigation(ContextualNavigationLog cnl) {
		cnLogs.add(cnl);
	}

}
