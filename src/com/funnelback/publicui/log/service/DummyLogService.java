package com.funnelback.publicui.log.service;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.log.ClickLog;

@Component
@Log
public class DummyLogService implements LogService {

	@Override
	public void logClick(ClickLog cl) {
		log.info(cl);
	}

}
