package com.funnelback.publicui.streamedresults;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;


/**
 * Separates a comma separated list that is used in a URL parameter e.g. list=foo,bar
 * 
 *
 */
public class CommaSeparatedListEditor extends PropertyEditorSupport {

    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        super.setValue(new CommaSeparatedList(parseValue(text)));
    }
    
    
    
    private List<String> parseValue(String s) {
        try {
            // We use RFC 4180 to parse the fields just like the CSV that would be returned.
            try(CSVParser csvParser = new CSVParser(new StringReader(s), CSVFormat.RFC4180)) {
                return csvParser.getRecords().stream()
                    .findFirst()
                    .map(record -> StreamSupport.stream(record.spliterator(), false)).orElse(Stream.<String>empty())
                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
