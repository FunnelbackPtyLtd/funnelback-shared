package com.funnelback.publicui.search.jmx;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.funnelback.publicui.search.service.IndexRepository;

@ManagedResource(description="Search monitor")
public class SearchMonitor {

	@Autowired
	private IndexRepository indexRepository;
	
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

	@ManagedOperation(description="Get the actual number of documents in a collection, as written in the .bldinfo file")
	@ManagedOperationParameters(
		{@ManagedOperationParameter(name="collectionId", description="Identifier of a collection")}
	)
	
	public long getNumberOfDocuments(String collectionId) {
		return Long.parseLong(indexRepository.getBuildInfoValue(collectionId, IndexRepository.BuildInfoKeys.Num_docs.toString()));
	}

	@ManagedOperation(description="Get the last updated date for this collection, as written in the index_time file")
	@ManagedOperationParameters(
			{@ManagedOperationParameter(name="collectionId", description="Identifier of a collection")}
	)	
	public Date getLastUpdatedDate(String collectionId) {
		return indexRepository.getLastUpdated(collectionId);
	}

	@ManagedOperation(description="Get the age of a collection, i.e. the number of seconds elapsed since the last collection update")
	@ManagedOperationParameters(
			{@ManagedOperationParameter(name="collectionId", description="Identifier of a collection")}
	)	
	public long getAge(String collectionId) {
		return (System.currentTimeMillis() - indexRepository.getLastUpdated(collectionId).getTime()) / 1000;
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
