package com.funnelback.publicui.api.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import com.funnelback.publicui.api.Search;
import com.funnelback.publicui.api.SearchAPIException;
import com.funnelback.publicui.api.SearchBuilder;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>This sample demonstrate a basic &quot;search shell&quot; which
 * runs queries against a given search service.</p>
 * 
 * <p>The URL of the search service must be passed as the single
 * argument to this program:<br />
 * <tt>SearchShell http://localhost:8080</tt>
 * </p>
 * 
 * <p>Special commands can be used, prefixed with a dot. Try
 * <tt>.help</tt> to obtain the list of commands.</p>
 *
 */
public class SearchShell {

	/**
	 * Number of documents to return.
	 */
	private static int num = 10;
	
	/**
	 * Collection to query. Defaults to funnelback_documentation
	 */
	private static String collection = "funnelback_documentation";
	
	/**
	 * URL of the search service
	 */
	private static String url;
	
	public static void main(String[] args) throws IOException {
		
		if (args.length != 1) {
			System.out.println("Usage: SearchShell <url of search service>");
			System.out.println("Ex: SearchShell http://localhost:8080");
			System.exit(1);
		} else {
			url = args[0];
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String read = null;
		
		System.out.println("Search shell. Enter your query terms, '.exit' or '.help'");
		System.out.print("?> ");
		
		while( !".exit".equals(read = br.readLine())) {
			if (read.startsWith(".")) {
				// Process an internal command
				processCommand(read);
			} else {
				// Run the query
				System.out.println("> Running query '" + read + "' on collection '" + collection + "' ...");
				try {
					// Build the Search API helper
					Search s = new SearchBuilder()
						.onSearchService(url)
						.onCollection(collection)
						.withQuery(read)
						.withNumRanks(num)
						.getSearch();
		
					// Submit the query
					SearchTransaction st = s.submit();
					
					if (st.getError() != null) {
						System.out.println("> An error has occured: " + st.getError().getReason() + " (" + st.getError().getAdditionalData() + ")");
					} else {
						if (SearchTransactionUtils.hasResults(st)) {
							System.out.println("> Number of matches: " + st.getResponse().getResultPacket().getResultsSummary().getFullyMatching());
						
							// Iterate over the results
							for(Result r: st.getResponse().getResultPacket().getResults()) {
								System.out.println(
										new DecimalFormat("00").format(r.getRank()) + ". "
										+ "(" + new DecimalFormat("0.000").format((float)r.getScore()/1000) + ") - "
										+ r.getTitle());
								System.out.println("    " + r.getLiveUrl());
							}
						} else {
							System.out.println("> No results found.");
						}
					}
				} catch (SearchAPIException sae) {
					System.err.println("An error occured");
					sae.printStackTrace(System.err);
				}
			}
			// Read next input.
			System.out.print("?> ");
		}
		
		System.out.println("Bye !");

	}
	
	/**
	 * Processes an internal command.
	 * @param cmd Command, starting with a dot.
	 */
	private static void processCommand(String cmd) {
		
		if (cmd.startsWith(".help")) {
			System.out.println("> Available commands:");
			System.out.println(">   .exit            : Exit");
			System.out.println(">   .collection <id> : Sets the collection to query");
			System.out.println(">   .num <num>       : Sets the maximum number of results to return");
			System.out.println(">   .info            : Display current settings");
		
		} else if (cmd.startsWith(".info")) {
			System.out.println("> Current collection is: " + collection);
			System.out.println("> Number of results: " + num);
		
		} else if (cmd.startsWith(".num")) {
			num = Integer.parseInt(cmd.substring(".num ".length()));
			System.out.println("> Number of results set to '" + num + "'");
		
		} else if (cmd.startsWith(".collection")) {
			collection = cmd.substring(".collection ".length());
			System.out.println("> Collection set to '" + collection + "'");
		}
	}
}
