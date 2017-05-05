package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class StandaloneParserTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.out.println("You must provide the folder containing XML files");
            System.exit(1);
        }
        
        File dir = new File(args[0]);
        if (!dir.isDirectory()) {
            System.out.println("Provided path '" + dir.getAbsolutePath() + "' is not a directory");
            System.exit(1);
        }
        System.out.println("Using folder: '" + dir.getAbsolutePath() + "'");
        
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        
        int errors = 0;
        StaxStreamParser parser = new StaxStreamParser();
        for (File f: files) {
            try {
                System.out.println("-- Parsing " + f.getAbsolutePath());
                ResultPacket rp = parser.parse(new FileInputStream(f), StandardCharsets.UTF_8, false);
                if (rp == null) {
                    errors++;
                    System.out.println("Parser result for file '" + f.getAbsolutePath() + "' is null");
                }
            } catch (Exception e) {
                System.out.println("Error while parsing '" + f.getAbsolutePath() + "'");
                e.printStackTrace();
                errors++;
            }
        }
        
        if (errors > 0) {
            System.out.println(errors + " errors encountered over " + files.length + " files");
            System.exit(1);
        } else {
            System.out.println("All " + files.length + " XML files were parsed successfully");
        }
        
    }
    
}
