package com.funnelback.publicui.search.service.log;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.Log;

import org.springframework.scheduling.annotation.Async;

import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;

@Log
public class DummyLogService implements LogService {

	@Override
	@Async
	public void logClick(ClickLog cl) {
		log.info(cl);
	}

	@Override
	@SneakyThrows(InterruptedException.class)
	@Async
	public void logContextualNavigation(ContextualNavigationLog cnl) {
		Thread.sleep(5000);
		log.info(cnl.toXml());
	}
}
