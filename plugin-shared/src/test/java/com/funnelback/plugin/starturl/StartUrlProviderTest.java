package com.funnelback.plugin.starturl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class StartUrlProviderTest {

    private StartUrlProviderContext context;

    @BeforeEach
    public void setup() {
        context = Mockito.mock(StartUrlProviderContext.class);
    }

    @Test
    public void testSuccessful() throws Exception {
        StartUrlProvider provider = getStartURLProvider(MyStartUrls.class);
        List<URL> result = provider.extraStartUrls(context);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void testBubbleUpException() {
        Assertions.assertThrows(RuntimeException.class, () -> getStartURLProvider(MyBrokenStartUrls.class).extraStartUrls(context));
    }

    private <S> StartUrlProvider getStartURLProvider (Class<S> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (clazz == null) {
            // Return null to allow testing code with a null object.
            return null;
        }

        S item = clazz.getDeclaredConstructor().newInstance();
        if (item instanceof StartUrlProvider) {
            return (StartUrlProvider) item;
        }

        throw new InstantiationException("Class is not of type 'StartUrlProvider'");
    }

    private static class MyStartUrls implements StartUrlProvider {
        @Override
        public List<URL> extraStartUrls(StartUrlProviderContext context) throws MalformedURLException {
            URL url1 = new URL("https", "squiz.net", 443, "url1");
            URL url2 = new URL("https", "squiz.net", 443, "url2");
            return List.of(url1, url2);
        }
    }

    private static class MyBrokenStartUrls implements StartUrlProvider {
        @Override
        public List<URL> extraStartUrls(StartUrlProviderContext context) throws RuntimeException {
            throw new RuntimeException("Plugin is throwing error");
        }
    }
}