package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * PADRE error data.
 * 
 * @since 11.0
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Error {

    /** User-friendly message. */
    @Getter private String userMsg;
    
    /** Technical message for the administrator. */
    @Getter private String adminMsg;
    
}
    
