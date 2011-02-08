package com.funnelback.publicui.search.model.collection;

import java.text.ParseException;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * A synonym (previously known as "query expansion"
 * @deprecated Synonyms will be done by PADRE, see FUN-3368.
 */
@Deprecated
public class Synonym {

	/** Separator used in config file line */
	private static final String SEPARATOR = "=";
	
	private enum MatchTypes { 
		TermByTerm('%'), FullQuery('+'), RegExp('~');
		
		@Getter private final char typeChar;
		
		private MatchTypes(char typeChar) {
			this.typeChar = typeChar;
		}
		
		public static MatchTypes fromChar(char typeChar) {
			for (MatchTypes type : MatchTypes.values()) {
				if (type.getTypeChar() == typeChar) {
					return type;
				}
			}
			throw new IllegalArgumentException("Unkonwn char type '" + typeChar + "'");
		}
	}
	
	/** Type of match to perform */
	@Getter final private MatchTypes type;
	
	/** Query to replace */
	@Getter final private String query;
	
	/** Pattern to use when using regular expression match type */
	@Getter final private Pattern queryPattern;
	
	/** Replacement (expansion) */
	@Getter final private String synonym;
	
	public Synonym(MatchTypes type, String query, String synonym) {
		this.type = type;
		this.query = query;
		this.synonym = synonym;
		
		if (MatchTypes.RegExp.equals(type)) {
			queryPattern = Pattern.compile(query);
		} else {
			queryPattern = null;
		}
	}
	
	/**
	 * Builds a Synonym from a config. file line such as:
	 * %=imigration=[immigration imigration]
	 *   or
     * +=DEST=[DEST "Department of Education Science and Training"]
     *   or
     * ~=w.*the=green
     *   ...
	 * @param configLine
	 */
	public static Synonym fromConfigLine(String configLine) throws ParseException {
		String[] fields = configLine.split(SEPARATOR);
		if (fields.length != 3) {
			throw new ParseException("Line doesn't contains 3 fields", 0);
		}
		
		if (fields[0].length() != 1) {
			throw new ParseException("First field should only contain 1 char", 0);
		}
		
		MatchTypes type = MatchTypes.fromChar(fields[0].charAt(0));
		String query = fields[1];
		String synonym = fields[2];
		
		return new Synonym(type, query, synonym);
	}
	
	/**
	 * Expands a query by applying the expansion rules
	 * @param query
	 * @return The expanded query, or the input string
	 */
	public String expandQuery(String queryTerms) {
		switch (type) {
		case TermByTerm:
			StringBuffer out = new StringBuffer();
			for(String term: queryTerms.split(" ")) {
				if (term.equals(query)) {
					out.append(query);
				} else {
					out.append(term);
				}
			}
			break;
		case FullQuery:
			if (queryTerms.equals(query)) {
				return synonym;
			}
			break;
		case RegExp:
			if (queryPattern.matcher(queryTerms).matches()) {
				return synonym;
			}
			break;
		}
		
		return queryTerms;		
	}
	
}
