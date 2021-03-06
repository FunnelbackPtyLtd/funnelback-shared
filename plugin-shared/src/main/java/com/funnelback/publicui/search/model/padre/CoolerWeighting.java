package com.funnelback.publicui.search.model.padre;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>Ranking weighting, defined using <code>-cool</code> command line
 * flags on the query processor.</p>
 * 
 * <p>Each weighting is defined by a short name and an identifier</p>
 * 
 * <p>This is used mostly when explain mode is enabled, for the
 * Content Optimiser.</p>
 * 
 * @since v12.4
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CoolerWeighting {
    
    /** Separator between the id and the name */
    private final static String SEP = ":";

    /** Short name, e.g. <em>offlink</em> */
    @Getter @Setter private String name;
    
    /** Identifier */
    @Getter @Setter private int id;
    
    @Override
    public String toString() {
        // Be aware that this String representation will be used by
        // the JSON serializer when a CoolerWeighting is used as a Map key.
        // See also fromJSON() below to adapt if you change the format
        return id+SEP+name;
    }
    
    /**
     * Used to deserialize a {@link CoolerWeighting} from a JSON value,
     * as represented by {@link #toString()}.
     * @param json JSON representation of a {@link CoolerWeighting}
     * @return The deserialized {@link CoolerWeighting}
     */
    @JsonCreator
    public static CoolerWeighting fromJSON(String json) {
        String[] kv = json.split(SEP);
        return new CoolerWeighting(kv[1], Integer.parseInt(kv[0]));
    }
    
}
