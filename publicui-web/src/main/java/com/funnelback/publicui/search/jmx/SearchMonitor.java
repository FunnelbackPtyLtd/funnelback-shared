package com.funnelback.publicui.search.jmx;

import java.util.Date;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description="Search monitor")
public class SearchMonitor {

	private long nbQueries = 0;
	private float avgPadreTime = -1;
	
	private final long startTime = new Date().getTime();
	
	@ManagedAttribute(description="Total number of queries processed")
	public long getNbQueries() {
		return nbQueries;
	}
	
	@ManagedAttribute(description="Number of seconds the service has run for")
	public long getUptime() {
		return (new Date().getTime() - startTime) / 1000;
	}
	
	@ManagedAttribute(description="Average queries per seconds rate")
	public float getAverageQueryPerSeconds() {
		return nbQueries / (float) getUptime();
	}
	
	@ManagedAttribute(description="Average PADRE processing time")
	public float getAveragePadreProcessingTime() {
		return avgPadreTime;
	}

	public synchronized void incrementQueryCount() {
		nbQueries++;
	}
	
	public synchronized void addResponseTime(long time) {
		if (avgPadreTime == -1) {
			// First time
			avgPadreTime = time;
		} else {
			avgPadreTime += time;
			avgPadreTime /= 2;
		}
	}
	
}
