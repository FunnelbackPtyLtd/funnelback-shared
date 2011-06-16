package com.funnelback.publicui.form.converter.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Log;

import com.funnelback.publicui.form.converter.Operation;

/**
 * <p>Removes &lt;s:CategoryRename&gt; and child tags.</p>
 */
@Log
public class CategoryRenameRemove implements Operation {

	@Override
	public String process(String in) {
		String out = in;
		
		Matcher m = Pattern.compile("<s:CategoryRename>(.*?)</s:CategoryRename>", Pattern.DOTALL).matcher(out);
		if (m.find()) {
			List<String[]> renames = new ArrayList<String[]>();
			StringBuffer sb = new StringBuffer();
			do {
				
				String from = null;
				Matcher mFrom = Pattern.compile("<s:From>(.*?)</s:From>").matcher(m.group(1));
				if (mFrom.find()) {
					from = mFrom.group(1);
				}
				
				String to = null;
				Matcher mTo = Pattern.compile("<s:To>(.*?)</s:To>").matcher(m.group(1));
				if (mTo.find()) {
					to = mTo.group(1);
				}
				
				if (from != null && to != null) {
					renames.add(new String[]{from, to});
				}
				
				m.appendReplacement(sb, "");
			} while(m.find());
			m.appendTail(sb);
			
			out = sb.toString();
	
			StringBuffer rulesMessage = new StringBuffer();
			for (String[] rule: renames) {
				rulesMessage.append(rule[0] + " => " + rule[1] + System.getProperty("line.separator"));
			}
			log.warn("<s:CategoryRename> tags have been removed. You'll need to implement category "
					+ "renaming using a faceted navigation transform script. The following rules have been found:\n"
					+ rulesMessage.toString());
		}
		
		return out;
	}

}
