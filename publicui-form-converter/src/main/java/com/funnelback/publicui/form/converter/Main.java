package com.funnelback.publicui.form.converter;

import lombok.extern.slf4j.Slf4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

@Slf4j
public class Main {

    
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("form-converter.xml");
    
        CliOptions opts = new CliOptions();
        JCommander cmd = new JCommander(opts);
        try {
            cmd.parse(args);
        } catch (ParameterException pe) {
            System.out.println("Error: " + pe.toString());
            cmd.usage();
            System.exit(-1);
        }
        
        if (opts.verbose) {
            Logger.getRootLogger().setLevel(Level.INFO);
        } else if (opts.veryVerbose) {
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        
        FormConverter converter = ctx.getBean(FormConverter.class);
        
        try {
            converter.run(opts);
        } catch (Throwable t) {
            log.error("Error", t);
        }
        
    }
    
}
