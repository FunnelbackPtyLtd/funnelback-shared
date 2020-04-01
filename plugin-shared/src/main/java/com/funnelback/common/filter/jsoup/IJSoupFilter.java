package com.funnelback.common.filter.jsoup;

import java.util.function.Consumer;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

/**
 * Interface for filtering steps applied by JSoupProcessingFilterProvider.
 * 
 * The chain of steps to apply can be configured with the 'filter.jsoup.classes'
 * collection.cfg parameter.
 * 
 * One instance of this class will be created per thread performing filtering
 * (to avoid unintended concurrency problems).
 */
public interface IJSoupFilter extends Runnable {
    
    /**
     * Perform any setup the filter may need before process is first called. Where possible
     * any time consuming work should be done here to avoid re-doing it for every document
     * in the collection.
     * 
     * The default implementation does nothing, which is appropriate for many filters.
     */
    default public void setup(SetupContext setup) {
        // Do nothing
    }
    
    /**
     * Implement this method to perform any desired processing on the filterContext.getDocument()
     * object before it is stored in the collection.
     * 
     * Modifications can be made in place, or by setting additionalMetadata values in filterContext.getAdditionalMetadata().
     * 
     * Since this method is called for every document, try to do any time consuming setup in the setup() method.
     * 
     * Note that this method will never be called in parallel, since a new instance of your filter
     * will be created for each thread. If you need to share state across all filter instances
     * you can do so by creating static variables/methods on your filter class and calling/using them
     * however, be aware then that you must then manage any concurrency safely yourself.
     */
    public void processDocument(FilterContext filterContext);
    
    /**
     * We provide a default run method, which is run when a groovy script is executed.
     * 
     * This method implements a very basic test harness that:
     * - Looks in the same directory as the groovy script file being run (and bails out if it's not under @groovy)
     * - Looks for files name scriptName-someTestName.test and scriptName-someTestName.expected in that directory
     * - Runs the filter in question on each scriptName-someTestName.test to produce scriptName-someTestName.actual
     * - Compares the expected and actual output, and produces basic information about the passes/failures.
     */
    @SuppressWarnings("unchecked")
    default public void run() {
        Class<?> testRunnerClass;
        try {
            testRunnerClass = this.getClass().getClassLoader().loadClass("com.funnelback.common.filter.jsoup.JsoupTestRunner");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can not run jsoup filter without $SEARCH_HOME/lib/java/all on the class path.");
        }
        
        Consumer<Class<? extends IJSoupFilter>> testRunner;
        try {
            testRunner = (Consumer<Class<? extends IJSoupFilter>>) testRunnerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        
        testRunner.accept(this.getClass());
    }
    
    public static final String TEST_SUFFIX = "test";
    public static final String EXPECTED_SUFFIX = "expected";
    public static final String ACTUAL_SUFFIX = "actual";

    /**
     * Compare two byte arrays, ignoring the difference between different line endings (\n, \r\n, \r).
     * 
     * Note - The current implementation is not efficient, so don't use this in performance critical code!
     */
    public static boolean compareByteArraysIgnoringLineEndings(byte[] a, byte[] b) {
        // ISO-8859-1 because it preserves all bytes
        String aString = new String(a, StandardCharsets.ISO_8859_1);
        String bString = new String(b, StandardCharsets.ISO_8859_1);
        
        // Convert Windows newlines into Unix ones for consistency
        aString = aString.replace("\r\n", "\n");
        bString = bString.replace("\r\n", "\n");

        // Yep, supporting old Mac OS
        aString = aString.replace("\r", "\n");
        bString = bString.replace("\r", "\n");
        
        return aString.equals(bString);
    }

}
