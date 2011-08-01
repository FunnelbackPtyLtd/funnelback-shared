package com.funnelback.contentoptimiser.processors.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class MetaInfo {
		@Getter @Setter private  String shortTitle;
		@Getter @Setter private  String longTitle;
		@Getter @Setter private  String improvementSuggestion;
		@Getter @Setter private  Integer threshold;
		
		public MetaInfo() {} // for VMs that only support pure java xstream
}
