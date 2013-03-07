package com.funnelback.publicui.search.model.padre;

public class PadreNative {

    public static class SizeOf {
        public static final byte FLOAT = 4;
        public static final byte INT = 4;
        public static final byte DOUBLE = 8;
        public static final byte CHAR = 1;
        
        // 3: Padding for struct alignment
        // 44: Size of buffer as said in build_spelling_index.h
        public static final short SUGGEST_T = FLOAT + CHAR + (44*CHAR) + 3;
        
        public static final short SPELL_INDEX_T = INT + INT;
    }

    
}
