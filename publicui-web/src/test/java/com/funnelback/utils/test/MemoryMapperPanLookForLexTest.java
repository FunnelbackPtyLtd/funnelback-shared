package com.funnelback.utils.test;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.utils.DefaultPanLookFactory;
import com.funnelback.utils.PanLook;


public class MemoryMapperPanLookForLexTest {

	File testFile;
	
	@Before
	public void setup() {
		testFile = new File("src/test/resources/utils/index.lex");
	}
	
	@Test
	public void testNotFoundEnd() throws IOException {
		String[] expected = {};
		String prefix = "ﬂzzzz";

		checkPanLook(expected, prefix);	
	}
	
	@Test
	public void testNotFoundStart() throws IOException {
		String[] expected = {};
		String prefix = "00";

		checkPanLook(expected, prefix);	
	}
	

	@Test
	public void testNotFoundMiddle() throws IOException {
		String[] expected = {};
		String prefix = "terrp";

		checkPanLook(expected, prefix);	
	}
	

	
	@Test
	public void testStart() throws IOException {
		String[] expected = {"01 d   56  28"};
		String prefix = "01";

		checkPanLook(expected, prefix);	
	}
	
	@Test
	public void testEnd() throws IOException {
		String[] expected = {"ﬂying   1  1"};
		String prefix = "ﬂying";

		checkPanLook(expected, prefix);	
	}

	@Test
	public void testThreeInTheMiddle() throws IOException {
		
		String[] expected ={"tent   20  8",
		"tent k   2  2",
		"tent t   6  3"};
		String prefix = "tent";

		checkPanLook(expected, prefix);	
	}

	private void checkPanLook(String[] expected, String prefix) throws IOException {
		int count = 0;
		PanLook panlook = new DefaultPanLookFactory().getPanLookForLex(testFile,prefix);
		for(String line : panlook) {
			Assert.assertEquals("pan-look should return the correct line(s) for prefix '" + prefix +"'",expected[count], line);
			count++;
		}
		panlook.close();
		Assert.assertEquals("pan-look should return the expected number of lines",expected.length, count);
	}
	
	
}
