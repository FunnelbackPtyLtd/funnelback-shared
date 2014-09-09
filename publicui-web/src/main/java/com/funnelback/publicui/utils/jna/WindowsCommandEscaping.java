package com.funnelback.publicui.utils.jna;

import java.util.List;

public class WindowsCommandEscaping {

    /** 
     * Escapes a list of arguments, and 
     * concatenates them with " " between.
     * The resulting string should be the ideal
     * single-string representation of a bunch
     * of arguments being passed to an external
     * program.
     * @param args  The arguments to escape
     * @return  A single escaped string representing
     * multiple arguments
     */
    public static String escapeArgumentsToString(List<String> args) {
        StringBuffer buf = new StringBuffer();
        for(String arg : args) {
            buf.append(argvQuote(arg));
            buf.append(" ");
        }
        return buf.toString().trim();
    }
    
    /** 
     * Escapes a string so that it can be used as an 
     * argument on the command line.
     * 
     * Algorithm from:
     * http://blogs.msdn.com/b/twistylittlepassagesallalike/archive/2011/04/23/everyone-quotes-arguments-the-wrong-way.aspx?Redirected=true
     * 
     * @param arg  The argument to escape
     * @return  The escaped argument
     */
    public static String argvQuote(String arg) {
        
        StringBuffer buf = new StringBuffer();
        
        buf.append('"');

        for(int c=0;;++c) {

            int nBackSlashes = 0;

            while ( c < arg.length() && arg.charAt(c) == '\\' ) {
                ++c;
                ++nBackSlashes;
            }

            if ( c == arg.length() ) {
                append(buf,nBackSlashes*2,'\\');
                break;
            } else if ( arg.charAt(c) == '"' ) {
                append(buf, nBackSlashes*2+1,'\\');
                buf.append(arg.charAt(c));
            } else {
                append(buf, nBackSlashes, '\\');
                buf.append(arg.charAt(c));
            }
        }
        buf.append('"');
        
        return buf.toString();
    }
    
    private static void append(StringBuffer buf, int count, char ch) {
        for(int i=0;i<count;i++) {
            buf.append(ch);
        }
    }   
}
