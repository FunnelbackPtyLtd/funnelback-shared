package com.funnelback.publicui.test.search.web.views.freemarker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.ParseRelativeDateMethod;

import freemarker.template.TemplateModelException;

public class ParseRelativeDateMethodTests extends AbstractMethodTest {

	@Test
	public void testNoReplacement() throws TemplateModelException {
		String result = (String) method.exec(buildStringArguments("abc def gh"));
		Assert.assertEquals("abc def gh", result);
		
		result = (String) method.exec(buildStringArguments("CURRENT_DAT e"));
		Assert.assertEquals("CURRENT_DAT e", result);
	}
	
	@Test
	public void testCurrentDate() throws TemplateModelException {
		String result = (String) method.exec(buildStringArguments("CURRENT_DATE"));
		Assert.assertEquals(new SimpleDateFormat("ddMMMyyyy").format(new Date()), result);

		result = (String) method.exec(buildStringArguments("CURRENT_DATE and query"));
		Assert.assertEquals(new SimpleDateFormat("ddMMMyyyy").format(new Date()) + " and query", result);
	}
	
	@Test
	public void testCurrentDateOperation() throws TemplateModelException {
		Calendar expected = Calendar.getInstance();
		expected.add(Calendar.YEAR, -2);
		
		String result = (String) method.exec(buildStringArguments("CURRENT_DATE - 2Y"));
		Assert.assertEquals(new SimpleDateFormat("ddMMMyyyy").format(expected.getTime()), result);

		expected = Calendar.getInstance();
		expected.add(Calendar.DAY_OF_MONTH, -3);
		result = (String) method.exec(buildStringArguments("CURRENT_DATE - 3D and query"));
		Assert.assertEquals(new SimpleDateFormat("ddMMMyyyy").format(expected.getTime()) + " and query", result);
	}
	
	@Override
	protected AbstractTemplateMethod buildMethod() {
		return new ParseRelativeDateMethod();
	}

	@Override
	protected int getRequiredArgumentsCount() {
		return 1;
	}

	@Override
	protected int getOptionalArgumentsCount() {
		return 0;
	}

}
