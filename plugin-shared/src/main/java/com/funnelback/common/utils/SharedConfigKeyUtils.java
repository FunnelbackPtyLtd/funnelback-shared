package com.funnelback.common.utils;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class SharedConfigKeyUtils {
    /**
     * Substitutes the wildcards into the key.
     *
     * The key's wildcards are places where between dots is a single '*'
     * e.g. foo.*.* is a key with two wildcards.
     *
     * @param key key in wildcard form ie. foo.*.*
     * @param wildcards  list of wildcard values to replace in key ie. List.of("bar", "baz")
     * @return key with replaced wildcards ie. foo.bar.baz
     */
    public static String getKeyWithWildcard(String key, List <String> wildcards) {
        Iterator <String> paramIterator = wildcards.iterator();
        Supplier <String> getNextParameter = () -> {
            if(paramIterator.hasNext()) {
                return paramIterator.next();
            }
            return "*"; // No parameter to sub in, assume * is literal
        };

        StringBuilder result = new StringBuilder();
        int offSet = 0;
        while (offSet < key.length()) {
            char c = key.charAt(offSet);
            if (c == '*') {
                char nextChar = 0;
                if (offSet + 1 < key.length()) { nextChar = key.charAt(offSet+1); }
                char lastChar = 0;
                if (offSet > 0) { lastChar = key.charAt(offSet-1); }

                if (0 == offSet && nextChar == '.') {
                    // *.foo
                    result.append(getNextParameter.get());
                } else if (offSet == key.length()-1 && (lastChar == '.' || lastChar == 0)) {
                    // foo.*
                    result.append(getNextParameter.get());
                } else if (nextChar == '.' && lastChar == '.') {
                    //foo.*.bar
                    result.append(getNextParameter.get());
                } else {
                    result.append('*');
                }
            } else {
                result.append(c);
            }
            offSet++;
        }

        if (paramIterator.hasNext()) {
            throw new RuntimeException("Too many wildcards provided for key " + key);
        }

        return result.toString();
    }
}
