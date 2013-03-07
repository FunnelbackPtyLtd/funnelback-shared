package com.funnelback.contentoptimiser.test.mock;

import java.util.HashMap;
import java.util.Map;

import com.funnelback.contentoptimiser.ConfigReader;

public class MockConfigReader implements ConfigReader<String> {

    int count= 0;
    
    @Override
    public Map<String, String> read(String file) {
        String[] a = {"one.txt","two.txt","three.txt"};
        Map<String, String> ret = new HashMap<String,String>();
        
        for(int i = count ; i < a.length ; i++) {
            ret.put(a[i], file);
        }
        count++;
        return ret;
    }

}
