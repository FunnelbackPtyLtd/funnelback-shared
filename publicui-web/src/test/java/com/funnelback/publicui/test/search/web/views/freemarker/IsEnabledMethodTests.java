package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.IsEnabledMethod;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class IsEnabledMethodTests extends AbstractMethodTest {

	@Test
	public void test() throws TemplateModelException {
		Assert.assertTrue((Boolean) method.exec(buildStringArguments("true")));
		Assert.assertTrue((Boolean) method.exec(buildStringArguments("on")));
		Assert.assertTrue((Boolean) method.exec(buildStringArguments("enabled")));
		Assert.assertTrue((Boolean) method.exec(buildStringArguments("yes")));
		
		Assert.assertFalse((Boolean) method.exec(buildStringArguments("false")));
		Assert.assertFalse((Boolean) method.exec(buildStringArguments("off")));
		Assert.assertFalse((Boolean) method.exec(buildStringArguments("disabled")));
		Assert.assertFalse((Boolean) method.exec(buildStringArguments("no")));
	}
	
	@Test
	public void testNullStringReturnsFalse() throws TemplateModelException {
		List<TemplateModel> arguments = new ArrayList<TemplateModel>();
		arguments.add(null);
		Assert.assertFalse((Boolean) method.exec(arguments));
	}
	
	@Override
	protected TemplateMethodModel buildMethod() {
		return new IsEnabledMethod();
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
