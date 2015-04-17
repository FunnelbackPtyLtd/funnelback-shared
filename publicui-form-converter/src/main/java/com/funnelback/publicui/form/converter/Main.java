package com.funnelback.publicui.form.converter;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

@Slf4j
public class Main {

    
    public static void main(String[] args) {
    
        CliOptions opts = new CliOptions();
        JCommander cmd = new JCommander(opts);
        try {
            cmd.parse(args);
        } catch (ParameterException pe) {
            System.out.println("Error: " + pe.toString());
            cmd.usage();
            System.exit(-1);
        }
        
        LoggerContext loggerCtx = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerCtx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 

        if (opts.verbose) {
        	loggerConfig.setLevel(Level.INFO);
        } else if (opts.veryVerbose) {
        	loggerConfig.setLevel(Level.DEBUG);
        }

        loggerCtx.updateLoggers(); 

    	ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("form-converter.xml");

        FormConverter converter = ctx.getBean(FormConverter.class);
        
        try {
            converter.run(opts);
        } catch (Throwable t) {
            log.error("Error", t);
        }
        
        ctx.close();
        
    }
    
}
