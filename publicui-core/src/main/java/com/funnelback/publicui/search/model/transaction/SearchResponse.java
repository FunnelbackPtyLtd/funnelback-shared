package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchHistory;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * <p>This class contains all the output data related to a search.</p>
 * 
 * <p>Most of this data is coming from PADRE, but there is also data
 * computed from the PADRE result set, such as facets.</p>
 *
 * @since 11.0
 */
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties({"rawPacket","translations"})
public class SearchResponse {

	/** The result packet coming from PADRE */
	@Getter @Setter private ResultPacket resultPacket;
	
	/**
	 * Raw XML packet as return by PADRE, for
	 * debugging purposes
	 */
	@XStreamOmitField
	@Getter @Setter private String rawPacket;
	
	/** PADRE return code (0 = success) */
	@Getter @Setter private int returnCode;
	
	/**
	 * Computed facets based on the PADRE
	 * result packet and collection configuration.
	 **/
	@Getter private final List<Facet> facets = new ArrayList<Facet>();
	
	/**
	 * Custom data placeholder allowing any arbitrary data to be
	 * stored by hook scripts.
	 */
	@Getter private final Map<String, Object> customData = new HashMap<String, Object>();
	
	/**
	 * Content Optimiser: URL comparison data.
	 */
	@Getter @Setter private ContentOptimiserModel optimiserModel;
	
	/**
	 * TextMiner: Entity/Definition/URL data.
	 * 
	 * @since 12.0
	 */	
	@Getter @Setter private EntityDefinition entityDefinition;
	
	/**
	 * Contains translation messages for the UI, for the locale
	 * given in the {@link SearchQuestion}.
	 * 
	 * @since 12.0
	 */
	@XStreamOmitField
	@Getter
	private final Map<String, String> translations = new HashMap<String, String>();
	
	/**
	 * Search history of the user for the current collection.
	 * 
	 * @since 12.4
	 */
	@Getter final private List<SearchHistory> searchHistory = new ArrayList<>();

	
	/**
	 * @return true if the {@link #resultPacket} is not null.
	 */
	public boolean hasResultPacket() { return resultPacket != null; }
}
