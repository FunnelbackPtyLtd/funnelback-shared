package com.funnelback.publicui.search.web.binding;

import java.beans.PropertyEditorSupport;

public class StringArrayFirstSlotEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		// FUN-4279: Account for more than one values
		if (text.contains(",")) {
			setValue(text.split(",")[0]);
		} else {
			setValue(text);
		}
	}
	
}
