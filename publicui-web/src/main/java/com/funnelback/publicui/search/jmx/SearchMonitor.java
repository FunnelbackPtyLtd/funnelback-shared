package com.funnelback.publicui.search.jmx;

import lombok.Setter;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description="Search monitor")
public class SearchMonitor {

	@Setter private long nbQueries;
	
	@ManagedAttribute(description="Total number of queries processed")
	public long getNbQueries() {
		return nbQueries;
	}
	
}
