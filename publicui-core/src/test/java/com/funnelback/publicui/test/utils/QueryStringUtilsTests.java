package com.funnelback.publicui.test.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.QueryStringUtils;

public class QueryStringUtilsTests {

	@Test
	public void testToString() {
		HashMap<String, List<String>> qs = new HashMap<String, List<String>>();
		
		qs.put("param1", Arrays.asList(new String[] {"single-value"}));
		qs.put("param2", Arrays.asList(new String[] {"first value", "second value"}));
		qs.put("param3", Arrays.asList(new String[] {"\n\t"}));
		qs.put("param4", null);
		qs.put("param5", Arrays.asList(new String[] {""}));
		
		Assert.assertEquals(
				"param1=single-value"
				+ "&param2=first+value&param2=second+value"
				+ "&param3=%0A%09"
				+ "&param4="
				+ "&param5=",
				QueryStringUtils.toString(qs, false));

		Assert.assertEquals(
				"?param1=single-value"
				+ "&param2=first+value&param2=second+value"
				+ "&param3=%0A%09"
				+ "&param4="
				+ "&param5=",
				QueryStringUtils.toString(qs, true));
	}
	
	@Test
	public void testToMap() {
		String input = "param1=value1"
			+ "&param2=first+value"
			+ "&param2=second%20value"
			+ "&param3=%0A%09"
			+ "&param4="
			+ "&param5=null";
			
		Map<String, List<String>> map = QueryStringUtils.toMap(input);
		
		Assert.assertEquals(map.get("param1"), Arrays.asList(new String[] {"value1"}));
		Assert.assertEquals(map.get("param2"), Arrays.asList(new String[] {"first value", "second value"}));
		Assert.assertEquals(map.get("param3"), Arrays.asList(new String[] {"\n\t"}));
		Assert.assertEquals(map.get("param4"), null);
		Assert.assertEquals(map.get("param5"), Arrays.asList(new String[] {"null"}));
		
		map = QueryStringUtils.toMap("?" + input, true);

		Assert.assertEquals(map.get("param1"), Arrays.asList(new String[] {"value1"}));
		Assert.assertEquals(map.get("param2"), Arrays.asList(new String[] {"first value", "second value"}));
		Assert.assertEquals(map.get("param3"), Arrays.asList(new String[] {"\n\t"}));
		Assert.assertEquals(map.get("param4"), null);
		Assert.assertEquals(map.get("param5"), Arrays.asList(new String[] {"null"}));
	}
	
}
