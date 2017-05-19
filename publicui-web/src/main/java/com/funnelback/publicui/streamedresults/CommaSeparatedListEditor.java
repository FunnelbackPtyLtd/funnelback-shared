package com.funnelback.publicui.streamedresults;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.function.StreamUtils;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CommaSeparatedListEditor extends PropertyEditorSupport {

    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        super.setValue(new CommaSeparatedList(parseValue(text)));
    }
    
    
    
    private List<String> parseValue(String s) {
        // TODO RFC 4180
        CSVReader csvReader = new CSVReader(new StringReader(s));
        try {
            return StreamUtils.ofNullable(csvReader.readNext()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
