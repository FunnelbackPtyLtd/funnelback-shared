package com.funnelback.publicui.web.binding;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.web.bind.ServletRequestDataBinder;

public class FieldTranslationServletRequestDataBinder extends ServletRequestDataBinder {

	public FieldTranslationServletRequestDataBinder(Object target) {
		super(target);
	}
	
	public FieldTranslationServletRequestDataBinder(Object target, String objectName) {
		super(target, objectName);
	}
	
	@Override
	protected void doBind(MutablePropertyValues mpvs) {
		for(int i=0; i<mpvs.size(); i++) {
			if (mpvs.getPropertyValues()[i].getName().equals("start_rank")) {
				PropertyValue pv = new PropertyValue("startRank", mpvs.getPropertyValues()[i].getValue());
				mpvs.setPropertyValueAt(pv, i);
			}
		}
		super.doBind(mpvs);
	}

}
