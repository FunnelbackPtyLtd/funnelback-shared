package com.funnelback.contentoptimiser;

import lombok.Getter;

public class Hint {
	@Getter
	String html;
	@Getter
	String link;
	@Getter
	String linkText;
	
	public Hint (String html,String link,String linkText) {
		this.html = html;
		this.link = link;
		this.linkText = linkText;
	}
}
