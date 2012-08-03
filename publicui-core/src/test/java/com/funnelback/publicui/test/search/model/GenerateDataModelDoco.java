package com.funnelback.publicui.test.search.model;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.BeanUtils;

import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.padre.QSup;
import com.funnelback.publicui.search.model.padre.QuickLinks.QuickLink;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.TierBar;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.xml.SearchXStreamMarshaller;

/**
 * <p>Generates a sample data model tree doco by requesting a running
 * Modern UI instance and using Java reflection to build the tree.</p>
 * 
 * @since v12.0
 */
public class GenerateDataModelDoco {

	/**
	 * List of classes to exclude from the tree
	 */
	private static List<Class<?>> CLASS_BLACKLIST = Arrays.asList(new Class<?>[] {
			Class.class,
			ContentOptimiserModel.class
			});
	
	/**
	 * Because of type erasure we can't get the underlying lists generic types
	 * so we need to specify them here.
	 */
	private static Map<String, Class<?>> LIST_OVERRIDES = new HashMap<String, Class<?>>();
	static {
		LIST_OVERRIDES.put("transaction.response.resultPacket.results", Result.class);
		LIST_OVERRIDES.put("transaction.question.collection.facetedNavigationLiveConfig.facetDefinitions", FacetDefinition.class);
		LIST_OVERRIDES.put("transaction.response.facets", Facet.class);
		LIST_OVERRIDES.put("transaction.response.resultPacket.QSups", QSup.class);
		LIST_OVERRIDES.put("transaction.response.resultPacket.bestBets", BestBet.class);
		LIST_OVERRIDES.put("transaction.response.resultPacket.contextualNavigation.categories", Category.class);
		LIST_OVERRIDES.put("transaction.response.resultPacket.results.quickLinks.quickLinks", QuickLink.class);
		LIST_OVERRIDES.put("transaction.response.resultPacket.tierBars", TierBar.class);
	}
	
	public static void main(String[] args) throws Exception {
		
		if (args.length != 1) {
			System.out.println("Usage: " + GenerateDataModelDoco.class.getName() + " <url_to_search.xml>");
			System.exit(1);
		}

		SearchXStreamMarshaller marshaller = new SearchXStreamMarshaller();
		marshaller.afterPropertiesSet();

		URLConnection cnx = new URL(args[0]).openConnection();
		InputStream is = cnx.getInputStream();
		SearchTransaction st = (SearchTransaction) marshaller.unmarshal(new StreamSource(is));
		is.close();
		
		StringBuilder sb = new StringBuilder();
		recurse(st.getClass(), SearchTransaction.class, "", "transaction", sb);
		
		System.out.println(sb.toString());
	}
	
	/**
	 * Recurses a class, extract properties and build a tree
	 * @param clazz Class of the current property to recurse
	 * @param parentClass Class of the parent object holding the property
	 * @param spaces Spaces for indentation
	 * @param path Current path in the data model
	 * @param sb Current data model tree
	 */
	private static void recurse(Class<?> clazz, Class<?> parentClass, String spaces, String path, StringBuilder sb) {
		 
		for (PropertyDescriptor pd: BeanUtils.getPropertyDescriptors(clazz)) {
			String propertyPath = path+"."+pd.getName();
			
			if (pd.getPropertyType().isPrimitive() || pd.getPropertyType().isArray()) {
				// Primitive or array, display property name + canonical type
				sb.append(spaces)
					.append("  <a data-path=\"").append(path+"."+pd.getName()).append("\" href=\"").append(getUrl(clazz, pd.getName())).append("\">")
					.append(pd.getName()).append("</a> (").append(pd.getPropertyType().getCanonicalName()).append(")")
					.append("<span class=\"path\"></span>")
					.append(System.getProperty("line.separator"));
				
			} else if (!CLASS_BLACKLIST.contains(pd.getPropertyType()) && pd.getPropertyType().getPackage().getName().startsWith("com.funnelback.publicui")) {
				// Modern UI property
				// Build link to class and recurse
				sb.append(spaces)
					.append("- <a data-path=\"").append(path+"."+pd.getName()).append("\" href=\"").append(getUrl(pd.getPropertyType())).append("\">").append(pd.getName()).append("</a>")
					.append("<span class=\"path\"></span>")
					.append(System.getProperty("line.separator"));
				recurse(pd.getPropertyType(), clazz, spaces + "  ", propertyPath, sb);
				
			} else if (LIST_OVERRIDES.containsKey(propertyPath)) {
				// Override for lists we can't get the underlying type due to type erasure
				sb.append(spaces)
					.append("- <a data-path=\"").append(path+"."+pd.getName()).append("\" href=\"").append(getUrl(LIST_OVERRIDES.get(propertyPath))).append("\">").append(pd.getName()).append("</a>")
					.append(" (List)")
					.append("<span class=\"path\"></span>")
					.append(System.getProperty("line.separator"));
				recurse(LIST_OVERRIDES.get(propertyPath), clazz, spaces + "  ", propertyPath, sb);
				
			} else if (!CLASS_BLACKLIST.contains(pd.getPropertyType())) {
				// Non publicui package object
				sb.append(spaces)
					.append("  <a data-path=\"").append(path+"."+pd.getName()).append("\" href=\"").append(getUrl(clazz, pd.getName())).append("\">").append(pd.getName())
					.append("</a> (").append(pd.getPropertyType().getSimpleName()).append(")")
					.append("<span class=\"path\"></span>")
					.append(System.getProperty("line.separator"));
			}
		}
	}
	
	/**
	 * Gets the relative URL for the page of a class
	 * @param clazz
	 * @return
	 */
	private static String getUrl(Class<?> clazz) {
		return clazz.getName().replace(".", "/").replace("$", ".") + ".html";
	}
	
	/**
	 * Gets the relative URL for the page of a class
	 * @param parentClass
	 * @param propertyName
	 * @return
	 */
	private static String getUrl(Class<?> parentClass, String propertyName) {
		return getUrl(parentClass) + "#" + propertyName;
	}
	
	
	
	
}
