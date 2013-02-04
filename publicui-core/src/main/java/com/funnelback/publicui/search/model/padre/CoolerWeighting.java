package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
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
public class CoolerWeighting {

	/** Short name, e.g. <em>offlink</em> */
	@Getter @Setter private String name;
	
	/** Identifier */
	@Getter @Setter private int id;
	
	/**
	 * <p>2 CoolerWeightings are considered equals
	 * if their name + id match.</p>
	 * 
	 * <p>Two different instances with the same name and id
	 * will be considered equal.</p>
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null) {
			CoolerWeighting other = (CoolerWeighting) obj;
			
			if (other.getId() == this.getId()) {
				return (other.getName() == null && this.getName() == null)
						|| other.getName().equals(this.getName());
			}
		}
		
		return false;
	}
	
	/**
	 * <p>Hash code based on the name + id properties.</p>
	 * 
	 * <p>Two different instances with the same name and id
	 * will have the same hash code.</p>
	 */
	@Override
	public int hashCode() {
		if (getName() != null) {
			return getName().hashCode() + getId();
		} else {
			return getId();
		}
	}
	
	@Override
	public String toString() {
		return id+":"+name;
	}
	
}
