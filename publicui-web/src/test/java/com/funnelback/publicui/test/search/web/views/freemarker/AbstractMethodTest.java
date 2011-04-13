package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.TagifyMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public abstract class AbstractMethodTest {

	protected TemplateMethodModel method;
	
	protected abstract TemplateMethodModel buildMethod();
	protected abstract int getRequiredArgumentsCount();
	protected abstract int getOptionalArgumentsCount();
	
	@Before
	public void before() {
		method = buildMethod();
	}
	
	@Test
	public void testMissingArguments() {
		try {
			method.exec(null);
			if (getRequiredArgumentsCount() > 0) {
				Assert.fail("Method shouldn't execute with null arguments");
			}
		} catch (NullPointerException npe) {
		} catch (TemplateModelException tme) {
			if (getRequiredArgumentsCount() > 0) {
				Assert.fail("Method should execute with null arguments");
			}
		}

		try {
			method.exec(new ArrayList<SimpleScalar>());
			if (getRequiredArgumentsCount() > 0) {
				Assert.fail("Method shouldn't execute with 0 arguments");
			}
		} catch (TemplateModelException tme) {
			if (getRequiredArgumentsCount() == 0) {
				Assert.fail("Method should execute with 0 arguments");
			}
		}
		
		if (getRequiredArgumentsCount() > 0) {
			try {
				ArrayList<SimpleScalar> arguments = new ArrayList<SimpleScalar>();
				for (int i=0; i<getRequiredArgumentsCount()-1; i++) {
					arguments.add(new SimpleScalar("argument " + i));
				}
				
				method.exec(arguments);
				Assert.fail("Method should fail with 1 missing argument");
			} catch (TemplateModelException tme) {
			}
		}
	}
	
	public static List<TemplateModel> buildStringArguments(String... str) {
		List<TemplateModel> out = new ArrayList<TemplateModel>();
		for (String s: str) {
			out.add(new SimpleScalar(s));
		}
		return out;
	}
}
