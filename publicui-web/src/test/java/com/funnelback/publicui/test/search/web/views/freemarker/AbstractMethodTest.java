package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public abstract class AbstractMethodTest {

	@Autowired
	private I18n i18n;
	
	protected TemplateMethodModel method;
	
	protected abstract AbstractTemplateMethod buildMethod();
	protected abstract int getRequiredArgumentsCount();
	protected abstract int getOptionalArgumentsCount();
	
	@Before
	public void before() {
		AbstractTemplateMethod m = buildMethod();
		m.setI18n(i18n);
		method = m;
		
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
			if (getRequiredArgumentsCount() <= 0) {
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
	
	public static SimpleSequence buildSequenceStringArguments(String... str) {
		SimpleSequence out = new SimpleSequence();
		for (String s: str) {
			out.add(s);
		}
		return out;
	}
	
	public static List<TemplateModel> buildArguments(TemplateModel... templateModel) {
		List<TemplateModel> out = new ArrayList<TemplateModel>();
		for (TemplateModel tm: templateModel) {
			out.add(tm);
		}
		return out;		
	}
}
