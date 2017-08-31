package com.funnelback.publicui.search.model.transaction.facet.order;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.google.common.collect.ImmutableSet;


public class ByLabelAsNumberComparator implements Comparator<Facet.CategoryValue> {
    
    private static final Set<Character> SPACE_CHARS_ALLOWED_IN_NUMBERS = knownNumberSpaces();

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
        for(startOfNumber = 0; startOfNumber < s.length(); startOfNumber++) {
            if(isDigit(s.charAt(startOfNumber))) {
                break;
            }
        }
        
        int lengthOfNumber = 1;
        
        // No digits found so it is not a number.
        if(startOfNumber >= s.length()) return null;
        
        if(startOfNumber > 0) {
            // Did we have a negatove sign before the digits (e.g. -0.1)
            if(s.charAt(startOfNumber - 1) == '-') {
                startOfNumber--;
                lengthOfNumber++;
            }
        }
        
        StringBuilder number = new StringBuilder();
        number.append(s.substring(startOfNumber, startOfNumber + lengthOfNumber));
        
        boolean hasDecimal = false;
        
        for(int i = startOfNumber + lengthOfNumber;  i < s.length(); i++) {
            char c = s.charAt(i);
            if(isDecimalPoint(c)) {
                if(hasDecimal) {
                    // Numbers can have at most one decimal place.
                    break;
                }
                hasDecimal = true;
                c = '.'; // Re-write decimal point to be one accepted by BigDecimal.
            } else if(isNumberSeparator(c) 
                    && i > 0 && isDigit(s.charAt(i - 1))
                    && i < s.length() - 1 && isDigit(s.charAt(i + 1))) {
                // we allow some space seperarators in numbers as long as it is a digit on either side.
                // this means 1, 1 is treated as 1 not 1.1
                continue;
            } else if(!isDigit(c)) {
                break;
            }
            number.append(c);
        }
        
        return new BigDecimal(number.toString());
    }
    
    boolean isDigit(char c) {
        return Character.isDigit(c);
    }
    
    boolean isDecimalPoint(char c) {
        // https://en.wikipedia.org/wiki/ISO_31-0#Numbers
        // ISO 31-0 (after Amendment 2) specifies that "the decimal sign 
        // is either the comma on the line or the point on the line". 
        // This follows resolution 10[1] of the 22nd CGPM, 2003.[2]
        return c == '.' || c == ',';
    }
    
    boolean isNumberSeparator(char c) {
        // https://en.wikipedia.org/wiki/ISO_31-0#Numbers
        // Numbers consisting of long sequences of digits can be made more readable by 
        // separating them into groups, preferably groups of three, separated by a small 
        // space. For this reason, ISO 31-0 specifies that such groups of digits should 
        // never be separated by a comma or point, as these are reserved for use as the 
        // decimal sign.
        return SPACE_CHARS_ALLOWED_IN_NUMBERS.contains(c);
    }
    
    private static Set<Character> knownNumberSpaces() {
        // Mostly from:
        // https://www.cs.tut.fi/~jkorpela/chars/spaces.html
        // http://www.unicode.org/charts/PDF/U2000.pdf
        // Note that java does not consider all of these white space, yet
        // in my browser they appear as white space.
            
        Set<Character> chars = new HashSet<>();
            
        chars.add(("\u0020".charAt(0)));   //SPACE    foo bar     Depends on font, typically 1/4 em, often adjusted
        chars.add(("\u2000".charAt(0)));   //EN QUAD  foo bar     1 en (= 1/2 em)
        chars.add(("\u2001".charAt(0)));   //EM QUAD  foo bar     1 em (nominally, the height of the font)
        chars.add(("\u2002".charAt(0)));   //EN SPACE     foo bar     1 en (= 1/2 em)
        chars.add(("\u2003".charAt(0)));   //EM SPACE     foo bar     1 em
        chars.add(("\u2004".charAt(0)));   //THREE-PER-EM SPACE   foo bar     1/3 em
        chars.add(("\u2005".charAt(0)));   //FOUR-PER-EM SPACE    foo bar     1/4 em
        chars.add(("\u2006".charAt(0)));   //SIX-PER-EM SPACE     foo bar     1/6 em
        chars.add(("\u2007".charAt(0)));   //FIGURE SPACE     foo bar     “Tabular width”, the width of digits
        chars.add(("\u2008".charAt(0)));   //PUNCTUATION SPACE    foo bar     The width of a period “.”
        chars.add(("\u2009".charAt(0)));   //THIN SPACE   foo bar     1/5 em (or sometimes 1/6 em)
        chars.add(("\u200A".charAt(0)));   //HAIR SPACE   foo bar     Narrower than THIN SPACE
        chars.add(("\u202F".charAt(0)));   //NARROW NO-BREAK SPACE    foo bar     Narrower than NO-BREAK SPACE (or SPACE)
        chars.add(("\u205F".charAt(0)));   //MEDIUM MATHEMATICAL SPACE    foo bar     4/18 em
        chars.add(("\u3000".charAt(0)));   //IDEOGRAPHIC SPACE    foo　bar     The width of ideographic (CJK) characters.
        return ImmutableSet.copyOf(chars);
    }

}
