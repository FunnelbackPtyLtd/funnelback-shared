package com.funnelback.filter.api.mock;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class FilterTestRunner {

    public static Result runTests(Class clazz) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out){
            @Override
            protected void printFailure(Failure each, String prefix) {
                System.out.println(prefix + ") " + each.getTestHeader());
                System.out.println("Failure in: " + each.getDescription().getMethodName());
                if(each.getException() instanceof AssertionError) {
                    System.out.println("\t" + each.getMessage());
                } else {
                    System.out.println(each.getTrace());
                }
            }
        }); 
        return junit.run(clazz);
       
    }
}
