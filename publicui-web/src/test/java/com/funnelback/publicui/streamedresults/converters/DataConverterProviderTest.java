package com.funnelback.publicui.streamedresults.converters;

import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.streamedresults.DataConverter;
import com.funnelback.publicui.utils.JsonPCallbackParam;

public class DataConverterProviderTest {

    private JSONDataConverter JSONDataConverter;
    private CSVDataConverter CSVDataConverter;
    private DataConverterProvider provider;
    
    @Before
    public void setup() throws Exception {
        this.JSONDataConverter = mock(JSONDataConverter.class);
        this.CSVDataConverter = mock(CSVDataConverter.class);
        provider = new DataConverterProvider(JSONDataConverter, CSVDataConverter);
    }
    
    @Test
    public void testCSV() {
        Assert.assertEquals(CSVDataConverter, provider.getDataConverterFromExtension("csv", Optional.empty(), true));
    }
    
    @Test
    public void testCSVNoHeader() {
        DataConverter<?> converter = provider.getDataConverterFromExtension("csv", Optional.empty(), false);
        Assert.assertTrue(converter instanceof NoHeaderAndFooterDataConverter);
        Assert.assertEquals(CSVDataConverter, ((NoHeaderAndFooterDataConverter) converter).getDataConverter());
    }
    
    @Test
    public void testJSON() {
        Assert.assertEquals(JSONDataConverter, provider.getDataConverterFromExtension("json", Optional.empty(), true));
    }
    
    @Test
    public void testJSONNoHeader() {
        DataConverter<?> converter = provider.getDataConverterFromExtension("json", Optional.empty(), false);
        Assert.assertTrue(converter instanceof NoHeaderAndFooterDataConverter);
        Assert.assertEquals(JSONDataConverter, ((NoHeaderAndFooterDataConverter) converter).getDataConverter());
    }
    
    @Test
    public void testJSONP() {
        Assert.assertTrue(provider.getDataConverterFromExtension("json", Optional.of(new JsonPCallbackParam("a")), true)
            instanceof JSONPDataConverter);
    }
    
    @Test
    public void testJSONPNoHeader() {
        DataConverter<?> converter = provider.getDataConverterFromExtension("json", Optional.of(new JsonPCallbackParam("a")), false);
        Assert.assertTrue(converter instanceof NoHeaderAndFooterDataConverter);
        Assert.assertEquals(JSONDataConverter, ((NoHeaderAndFooterDataConverter) converter).getDataConverter());
    }
    
}
