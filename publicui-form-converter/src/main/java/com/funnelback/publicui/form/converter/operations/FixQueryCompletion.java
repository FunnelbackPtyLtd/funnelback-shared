package com.funnelback.publicui.form.converter.operations;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Ensures that the query completion URL (padre-qs)
 * uses the Perl context root.
 */
public class FixQueryCompletion implements Operation {

	@Override
	public String process(String in) {
		
		return in.replace("<@s.cfg>query_completion.program", "${SearchPrefix}<@s.cfg>query_completion.program");
	}

}
