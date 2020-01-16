package com.funnelback.publicui.streamedresults.converters;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.streamedresults.DataConverter;
import com.funnelback.publicui.utils.JsonPCallbackParam;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class DataConverterProvider {
    
    @Autowired
    private JSONDataConverter JSONDataConverter;
    
    @Autowired
    private CSVDataConverter CSVDataConverter;

    /**
     * Gets the data converter to use based on the extension.
     * 
     * <p>Note that the return type is of object this is because I can't seem to do:
     * DataConverter<?> converter = getDataConverterFromExtension("");
     * converter.writeHead(convert.getWritter()); // here is the issue.
     * Java doesn't understand that the type returned by convert.getWritter() is the
     * same type as what is expected by converter.writeHead().</p>
     * 
     * @param ext
     * @param jsonPCallBack Used to specify the name of the function to wrap the JSON in.
     * @param headerAndFooter Specifies if the header and footer are wanted.
     * @return
     */
    public DataConverter<?> getDataConverterFromExtension(String ext, 
            Optional<JsonPCallbackParam> jsonPCallBack,
            boolean headerAndFooter) {
        Function<DataConverter<?>, DataConverter<?>> wrapWithNoHeadersIfNeeded = c -> {
            if(headerAndFooter) return c;
            return new NoHeaderAndFooterDataConverter<>(c);
        };
        
        if(ext.equals("json")) {
            // If the header and footer are not wanted then what is added by JSONP is not wanted, so
            // we just don't make use of it.
            if(jsonPCallBack.isPresent() && headerAndFooter) {
                return new JSONPDataConverter(jsonPCallBack.get(), this.JSONDataConverter);
            }
            return wrapWithNoHeadersIfNeeded.apply(this.JSONDataConverter);
        }
        
        if(ext.equals("csv")) {
            return wrapWithNoHeadersIfNeeded.apply(this.CSVDataConverter);
        }
        
        return null;
    }
}
