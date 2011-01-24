package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import lombok.Getter;

public class PadreForkingException extends Exception {

	private static final long serialVersionUID = 1L;

	private String message;
	private Integer returnCode;
	@Getter private String output;
	
	public PadreForkingException(int returnCode, String output) {
		this.returnCode = returnCode;
		this.output = output;
	}
	
	public PadreForkingException(String message, Throwable cause) {
		super(cause);
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		if (message != null) {
			return message;
		}
		
		StringBuffer out = new StringBuffer("PADRE execution failed.");
		if (returnCode != null) {
			out.append(" Return code is: '" + returnCode + "'.");
		}
		if (output != null) {
			out.append("Program output is '" + output + "'.");
		}
		
		return out.toString();
	}
	
}
