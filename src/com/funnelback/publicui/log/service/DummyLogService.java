package com.funnelback.publicui.log.service;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.Log;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;

@Service
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
