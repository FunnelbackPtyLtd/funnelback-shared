package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.views.freemarker.AbstractTemplateMethod;
import com.funnelback.publicui.search.web.views.freemarker.TruncateURLMethod;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TruncateURLMethodTests extends AbstractMethodTest {
	
	@Test
	public void testTooShort() throws TemplateModelException {
		ArrayList<TemplateModel> arguments = new ArrayList<TemplateModel>();
		arguments.add(new SimpleScalar("http://too.short.com/folder/"));
		arguments.add(new SimpleNumber(80));
		
		SimpleScalar result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("http://too.short.com/folder/", result.getAsString());
		
		arguments.clear();
		arguments.add(new SimpleScalar("\\\\server\\share\\file.ext"));
		arguments.add(new SimpleNumber(80));

		result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("\\\\server\\share\\file.ext", result.getAsString());

	}
	
	@Test
	public void test() throws TemplateModelException {
		ArrayList<TemplateModel> arguments = new ArrayList<TemplateModel>();
		arguments.add(new SimpleScalar("http://thisisalongurl.com/folder1/folder2/folder3/file.ext"));
		arguments.add(new SimpleNumber(36));
		
		SimpleScalar result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("http://thisisalongurl.com/folder1<br/>/folder2/folder3/file.ext", result.getAsString());

		arguments.clear();
		arguments.add(new SimpleScalar("\\\\thisisalongurl.com\\folder1\\folder2\\folder3\\file.ext"));
		arguments.add(new SimpleNumber(36));

		result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("\\\\thisisalongurl.com\\folder1\\folder2<br/>\\folder3\\file.ext", result.getAsString());

	}

	@Test
	public void testTooLong() throws TemplateModelException {
		ArrayList<TemplateModel> arguments = new ArrayList<TemplateModel>();
		arguments.add(new SimpleScalar("http://thisisalongurl.com/folder1/folder2/folder3/file.ext"));
		arguments.add(new SimpleNumber(5));
		
		SimpleScalar result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("ht\u2026xt", result.getAsString());

		arguments.clear();
		arguments.add(new SimpleScalar("\\\\thisisalongurl.com\\folder1\\folder2\\folder3\\file.ext"));
		arguments.add(new SimpleNumber(5));

		result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("\\\u2026ext", result.getAsString());
	}
	
	@Test
	public void testSecondLineTooLong() throws TemplateModelException {
		ArrayList<TemplateModel> arguments = new ArrayList<TemplateModel>();
		arguments.add(new SimpleScalar("http://thisisalongurl.com/folder1/folder2/folder3/file.ext"));
		arguments.add(new SimpleNumber(16));
		
		SimpleScalar result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("http:/<br/>/\u2026/file.ext", result.getAsString());

		arguments.clear();
		arguments.add(new SimpleScalar("\\\\thisisalongurl.com\\folder1\\folder2\\folder3\\file.ext"));
		arguments.add(new SimpleNumber(16));

		result = (SimpleScalar) method.exec(arguments);
		Assert.assertEquals("\\\\thisi\u2026file.ext", result.getAsString());

	}

	@Override
	protected AbstractTemplateMethod buildMethod() {
		return new TruncateURLMethod();
	}

	@Override
	protected int getRequiredArgumentsCount() {
		return 2;
	}

	@Override
	protected int getOptionalArgumentsCount() {
		return 0;
	}


}
