package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.ParseRangeMethod;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;

public class ParseRangeMethodTests extends AbstractMethodTest {

	@Test
	public void testNumericRange() throws TemplateModelException {
		SimpleHash result = (SimpleHash) method.exec(buildStringArguments("1..30"));
		Assert.assertEquals(1, ((SimpleNumber) result.get(ParseRangeMethod.START)).getAsNumber());
		Assert.assertEquals(30, ((SimpleNumber) result.get(ParseRangeMethod.END)).getAsNumber());

		result = (SimpleHash) method.exec(buildStringArguments("13..5"));
		Assert.assertEquals(13, ((SimpleNumber) result.get(ParseRangeMethod.START)).getAsNumber());
		Assert.assertEquals(5, ((SimpleNumber) result.get(ParseRangeMethod.END)).getAsNumber());
	}
	
	@Test
	public void testCurrentYear() throws TemplateModelException {
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		
		SimpleHash result = (SimpleHash) method.exec(buildStringArguments("CURRENT_YEAR..CURRENT_YEAR-2"));		
		Assert.assertEquals(currentYear, ((SimpleNumber) result.get(ParseRangeMethod.START)).getAsNumber());
		Assert.assertEquals(currentYear-2, ((SimpleNumber) result.get(ParseRangeMethod.END)).getAsNumber());

		result = (SimpleHash) method.exec(buildStringArguments("CURRENT_YEAR+5..CURRENT_YEAR-3"));		
		Assert.assertEquals(currentYear+5, ((SimpleNumber) result.get(ParseRangeMethod.START)).getAsNumber());
		Assert.assertEquals(currentYear-3, ((SimpleNumber) result.get(ParseRangeMethod.END)).getAsNumber());

		result = (SimpleHash) method.exec(buildStringArguments("CURRENT_YEAR+2..CURRENT_YEAR"));		
		Assert.assertEquals(currentYear+2, ((SimpleNumber) result.get(ParseRangeMethod.START)).getAsNumber());
		Assert.assertEquals(currentYear, ((SimpleNumber) result.get(ParseRangeMethod.END)).getAsNumber());
	}
	
	@Override
	protected AbstractTemplateMethod buildMethod() {
		return new ParseRangeMethod();
	}

	@Override
	protected int getRequiredArgumentsCount() {
		return 1;
	}

	@Override
	protected int getOptionalArgumentsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
