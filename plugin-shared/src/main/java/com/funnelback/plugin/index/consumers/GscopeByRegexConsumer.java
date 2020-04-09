package com.funnelback.plugin.index.consumers;

@FunctionalInterface
public interface GscopeByRegexConsumer {

    /**
     * A plugin may call this to supply a gscope which should be set when the regular expression matches the URL.
     * 
     * @param gscopeName The name the gscope to set. Limited to ASCII alpha numeric 
     * characters and must be no longer than 64 characters. Additionally gscope names which start 
     * with 'Fun' in any upper or lower case form are reserved for internal use.
     * @param perlRegularExpression The regular expression that will be matched against the URL.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    void applyGscopeWhenRegexMatches(String gscopeName, String perlRegularExpression)
        throws IllegalArgumentException;
}
