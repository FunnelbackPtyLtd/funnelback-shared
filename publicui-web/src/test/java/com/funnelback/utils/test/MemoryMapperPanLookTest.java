package com.funnelback.utils.test;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.contentoptimiser.utils.DefaultPanLookFactory;
import com.funnelback.contentoptimiser.utils.PanLook;


public class MemoryMapperPanLookTest {

    File testFile;
    
    @Before
    public void setup() {
        if(System.getProperty("os.name").contains("Windows")) {
            testFile = new File("src/test/resources/utils/index.distilled.windows.txt");
        } else {
            testFile = new File("src/test/resources/utils/index.distilled");
        }
    }
    
    
    @Test
    public void testNotFoundEnd() throws IOException {
        String[] expected = {};
        String prefix = "prefixnotfound";

        checkPanLook(expected, prefix);    
    }
    
    @Test
    public void testNotFoundStart() throws IOException {
        String[] expected = {};
        String prefix = "0000000 ";

        checkPanLook(expected, prefix);    
    }
    

    @Test
    public void testNotFoundMiddle() throws IOException {
        String[] expected = {};
        String prefix = "00000011";

        checkPanLook(expected, prefix);    
    }
    

    
    @Test
    public void testStart() throws IOException {
        String[] expected = {"00000000 00000029 [k2]Entire play"};
        String prefix = "00000000";

        checkPanLook(expected, prefix);    
    }
    
    @Test
    public void testEnd() throws IOException {
        String[] expected = {"00000029 00000003 [k2]King Lear",
                "00000029 00000005 [k2]King Lear"};
        String prefix = "00000029";

        checkPanLook(expected, prefix);    
    }

    @Test
    public void testThreeInTheMiddle() throws IOException {
        String[] expected = {"00000021 00000020 [k2]Next scene",
                "00000021 00000022 [k2]Previous scene",
                "00000021 00000029 [k2]The French camp near Dover"};
        String prefix = "00000021";

        checkPanLook(expected, prefix);    
    }


    private void checkPanLook(String[] expected, String prefix) throws IOException {
        int count = 0;
        PanLook panlook = new DefaultPanLookFactory().getPanLook(testFile,prefix);
        for(String line : panlook) {
            Assert.assertEquals("pan-look should return the correct line(s) for prefix '" + prefix +"'",expected[count], line);
            count++;
        }
        panlook.close();
        Assert.assertEquals("pan-look should return the expected number of lines",expected.length, count);
    }
    
    
}
