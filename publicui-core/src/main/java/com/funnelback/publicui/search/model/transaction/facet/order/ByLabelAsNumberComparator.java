package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.ibm.icu.math.BigDecimal;

public class ByLabelAsNumberComparator implements Comparator<Facet.CategoryValue> {

    @Override
    public int compare(CategoryValue o1, CategoryValue o2) {
        BigDecimal number1 = extractFirstNumber(o1.getLabel());
        BigDecimal number2 = extractFirstNumber(o2.getLabel());
        
        return Comparator.nullsLast(Comparator.<BigDecimal>naturalOrder())
                .compare(number1, number2);
    }
    
    /**
     * Extracts the first decimal number in the given string or returns null.
     * <p>This counts '.1' as number `1/10`. This will also supports 
     * negative numbers.</p>
     * @param s
     * @return
     */
    BigDecimal extractFirstNumber(String s) {
        int startOfNumber;
        boolean hasDecimal = false;
        for(startOfNumber = 0; startOfNumber < s.length(); startOfNumber++) {
            if(isDigit(s.charAt(startOfNumber))) {
                break;
            }
        }
        
        int lengthOfNumber = 1;
        
        // No digits found so it is not a number.
        if(startOfNumber >= s.length()) return null;
        
        
        if(startOfNumber > 0) {
            // Did we have a decimal before the digit?
            if(s.charAt(startOfNumber - 1) == '.') {
                startOfNumber--;
                lengthOfNumber++;
                hasDecimal = true;
            }
            
            // Did we have a negatove sign before the digit or decimal (e.g. -.1)
            if(s.charAt(startOfNumber - 1) == '-') {
                startOfNumber--;
                lengthOfNumber++;
            }
        }
        
        for(; startOfNumber + lengthOfNumber < s.length(); lengthOfNumber++) {
            char c = s.charAt(startOfNumber + lengthOfNumber);
            if(c == '.') {
                if(hasDecimal) {
                    // Numbers can have at most one decimal place.
                    break;
                } else {
                    hasDecimal = true;
                }
            } else if(!isDigit(c)) {
                break;
            }
        }
        
        // Cut out the number.
        String number = s.substring(startOfNumber, startOfNumber + lengthOfNumber);
        return new BigDecimal(number);
    }
    
    boolean isDigit(char c) {
        return ('0' <= c && c <='9');
    }

}
