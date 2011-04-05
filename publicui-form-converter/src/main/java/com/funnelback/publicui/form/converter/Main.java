package com.funnelback.publicui.form.converter;

import lombok.extern.slf4j.Log;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

@Log
public class Main {

	
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("form-converter.xml");
		
		CliOptions opts = new CliOptions();
		JCommander cmd = new JCommander(opts);
		try {
			cmd.parse(args);
		} catch (ParameterException pe) {
			cmd.usage();
			System.exit(-1);
		}
		
		FormConverter converter = ctx.getBean(FormConverter.class);
		
		try {
			converter.run(opts);
		} catch (Throwable t) {
			log.error("Error", t);
		}
		
	}
	
}
