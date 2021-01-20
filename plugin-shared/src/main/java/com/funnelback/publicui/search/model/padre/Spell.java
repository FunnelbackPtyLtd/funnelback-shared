package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>A spelling suggestion</p>
 * 
 * @since 11.0
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Spell {

    @Getter @Setter private String url;
    @Getter @Setter private String text;
    
}
