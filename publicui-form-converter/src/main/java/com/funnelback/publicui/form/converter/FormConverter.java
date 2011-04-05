package com.funnelback.publicui.form.converter;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Log;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log
public class FormConverter {

	@Resource(name="steps")
	private List<Operation> steps;
	
	public void run(CliOptions opts) throws IOException {
		
		String input = FileUtils.readFileToString(opts.input);
		log.info("Processing file '" + opts.input.getAbsolutePath() + "'");
		
		for (Operation o: steps) {
			log.info("Applying operation " + o.getClass().getSimpleName());
			input = o.process(input);
		}
		
		log.info("Writing converted file to '" + opts.output.getAbsolutePath() + "'");
		FileUtils.writeStringToFile(opts.output, input);
	}

}
