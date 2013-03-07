package com.funnelback.publicui.form.converter;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FormConverter {

    @Resource(name="steps")
    private List<Operation> steps;
    
    public void run(CliOptions opts) throws IOException {
        
        String input = FileUtils.readFileToString(opts.input);
        log.warn("Processing file '" + opts.input.getAbsolutePath() + "'");
        
        for (Operation o: steps) {
            log.debug("Applying operation " + o.getClass().getSimpleName());
            input = o.process(input);
        }
        
        log.warn("Writing converted file to '" + opts.output.getAbsolutePath() + "'");
        FileUtils.writeStringToFile(opts.output, input);
    }

}
