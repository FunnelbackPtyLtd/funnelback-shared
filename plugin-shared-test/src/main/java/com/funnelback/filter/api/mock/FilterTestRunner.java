package com.funnelback.filter.api.mock;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class FilterTestRunner {

    /**
     * Runs junit tests that are found on the given class
     * 
     * @param classWithJunitTests
     * @return
     */
    public static Result runTests(Class<?> classWithJunitTests) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new org.junit.internal.TextListener(System.out){
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
        return junit.run(classWithJunitTests);
       
    }
}
