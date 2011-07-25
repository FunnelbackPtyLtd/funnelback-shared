package com.funnelback.publicui.search.web.exception;

import lombok.Getter;

import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * Thrown when a {@link SearchController.ViewTypes} is not found,
 * i.e. when someone requests <code>/search.something</code>
 */
public class ViewTypeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * View type which caused the exception
	 */
	@Getter private final String viewType;
	
	public ViewTypeNotFoundException(String viewType) {
		super("View type '"+viewType+"' not found");
		this.viewType = viewType;		
	}
	
}
