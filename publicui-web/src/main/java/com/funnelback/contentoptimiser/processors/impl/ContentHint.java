package com.funnelback.contentoptimiser.processors.impl;

import lombok.Getter;
import lombok.Setter;


public class ContentHint implements Comparable<ContentHint>{
	@Getter private final String hintText;
	@Getter private final double scoreEstimate;
	
	@Getter @Setter private int bucket;

	public ContentHint(String hintText, double scoreEstimate, double percentOfDocuments) {
		this.hintText = hintText;
		this.scoreEstimate = scoreEstimate;
		if(percentOfDocuments < 0.001) {
			bucket = (int) Math.floor(Math.log10(percentOfDocuments));
		} else {
			bucket = 0;
		}
	}
	
	public ContentHint(String hintText, double scoreEstimate) {
		this.hintText = hintText;
		this.scoreEstimate = scoreEstimate;
		this.bucket = 0;
	}

	@Override
	public int compareTo(ContentHint that) {
		int bucketCompare = new Integer(that.getBucket()).compareTo(this.bucket);
		if(bucketCompare != 0) return bucketCompare;
			
		return new Double(that.getScoreEstimate()).compareTo(this.scoreEstimate) ;
		
	} 
	
}
