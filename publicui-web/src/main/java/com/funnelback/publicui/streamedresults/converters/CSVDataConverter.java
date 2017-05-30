package com.funnelback.publicui.streamedresults.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.streamedresults.DataConverter;


@Component
public class CSVDataConverter implements DataConverter<CSVPrinter>{

    @Override
    public String getContentType() {
        return "text/csv; charset=utf-8";
    }

    @Override
    public CSVPrinter createWriter(OutputStream outputStream) throws IOException {
        //RFC 4180
        CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), CSVFormat.DEFAULT);
        return csvPrinter;
    }

    @Override
    public void writeHead(List<String> fieldNames, CSVPrinter writer) throws IOException {
        writer.printRecord(fieldNames);
    }

    @Override
    public void writeRecord(List<String> fieldNames, List<Object> values, CSVPrinter writer) throws IOException {
        List<String> strings = new ArrayList<>(values.size());
        for(int i = 0; i < values.size(); i++) {
            
            Object value = values.get(i);
            // By default search.json appears to be giving dates as milliseconds since January 1, 1970, 00:00:00 GMT
            // so we do the same here for consistency.
            if(value != null && values.get(i) instanceof Date) {
                Date date = (Date) value;
                strings.add(date.getTime() + "");
            } else {
                strings.add(Optional.ofNullable(value).map(Object::toString).orElse(""));
            }
        }
        writer.printRecord(strings);
        
    }

    @Override
    public void writeSeperator(CSVPrinter writer) throws IOException {
        
    }

    @Override
    public void writeFooter(CSVPrinter writer) throws IOException {
    }

    @Override
    public void finished(CSVPrinter writer) throws IOException {
        writer.close();
    }
}