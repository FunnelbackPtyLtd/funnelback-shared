package com.funnelback.publicui.search.service.suggest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.RequiredArgsConstructor;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.PadreNative;
import com.funnelback.publicui.search.model.transaction.Suggestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.Suggester;

/**
 * Prototype of Java suggester that read the <code>.autoc</code>
 * files directly.
 * 
 * @deprecated Not compatible with scoped suggestions. Use {@link LibQSSuggester} instead.
 */
@Deprecated
public class JavaSuggester implements Suggester {

	private static final int SIZEOF_HEADER = PadreNative.SizeOf.INT*3 + PadreNative.SizeOf.DOUBLE;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public List<Suggestion> suggest(Collection c, String profileId, String partialQuery,
			int numSuggestions, Sort sort) {
		
		SortedSet<Suggestion> suggestions = new TreeSet<Suggestion>(new Suggestion.ByWeightComparator(.5f, partialQuery.length()));
		FileInputStream fis = null;
		
		try {
			File suggestFile = new File(c.getConfiguration().getCollectionRoot()
				+ File.separator + DefaultValues.VIEW_LIVE
				+ File.separator + DefaultValues.FOLDER_IDX,
				DefaultValues.INDEXFILES_PREFIX + ".suggest");
			fis = new FileInputStream(suggestFile);
			
			FileChannel channel = fis.getChannel();
			
			ByteBuffer headerBuf = ByteBuffer.allocate(SIZEOF_HEADER).order(ByteOrder.LITTLE_ENDIAN);
			channel.read(headerBuf);
			headerBuf.rewind();
			
			SuggestHeader header = SuggestHeader.fromBytes(headerBuf);
			if (header.format != SuggestHeader.FORMAT) {
				throw new IllegalStateException();
			}
			
			ByteBuffer indexBuf = ByteBuffer.allocate(header.indexSize * PadreNative.SizeOf.SPELL_INDEX_T).order(ByteOrder.LITTLE_ENDIAN);
			channel.read(indexBuf);
			indexBuf.rewind();
			
			char queryChar = partialQuery.charAt(0);
			
			int UNCHANGED = Integer.MAX_VALUE;
			int from=0;
			int to = UNCHANGED;
			
			for (int i=0; i<header.indexSize; i++) {
				int keyChar = indexBuf.getInt();
				int length = indexBuf.getInt();
				
				if (keyChar < queryChar) from = i;
				else if (keyChar == queryChar) {
					if (length < 0) from = i;
					else if (length == 0) {
						if ( to == UNCHANGED) from = i;
						to = i;
					} else {
						if (to == UNCHANGED) to = i;
					}
				} else {
					if (to == UNCHANGED) to = i;
				}
			}
	
			float step = header.suggestionElements / header.indexSize;
			double seekPosition = Math.floor(step * (float) from);
			
			channel.position(channel.position() + ((long) (seekPosition-1) * PadreNative.SizeOf.SUGGEST_T));
			
			
			
			ByteBuffer suggestionsbuf = ByteBuffer.allocate(PadreNative.SizeOf.SUGGEST_T*1024).order(ByteOrder.LITTLE_ENDIAN);
			
			boolean reached = false;
			while(!reached) {
				int read = channel.read(suggestionsbuf);
				if (read <= 0) {
					break;
				}
				
				int numSuggestionsRead = read / PadreNative.SizeOf.SUGGEST_T;			
				suggestionsbuf.rewind();
				
				for (int i=0; i<numSuggestionsRead; i++) {
					Suggestion s = fromBytes(suggestionsbuf);
					char suggestionChar = s.getDisplay().charAt(0);
					if (suggestionChar > queryChar) {
						reached = true;
						break;
					} else if ( suggestionChar == queryChar && s.getDisplay().startsWith(partialQuery) && s.getDisplay().length() > partialQuery.length()) {
						suggestions.add(s);	
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to obtain suggestions", ioe);
		} finally {
			IOUtils.closeQuietly(fis);
		}
		
		return new ArrayList<Suggestion>(suggestions);
	}

	private static Suggestion fromBytes(ByteBuffer buf) {
		float weight = buf.getFloat();
		char length = (char) buf.get();
		char[] letters = new char[length];
		for (int i=0; i<length; i++) {
			letters[i] = (char) buf.get();
		}
		buf.position(buf.position()+PadreNative.SizeOf.SUGGEST_T-(PadreNative.SizeOf.FLOAT + PadreNative.SizeOf.CHAR+length));

		Suggestion s = new Suggestion();
		s.setWeight(weight);
		s.setDisplay(new String(letters));
		return s;
	}
	
	@RequiredArgsConstructor
	private static class SuggestHeader {
		public static final int FORMAT = 2;
		
		public final int format;
		public final int indexSize;
		public final int suggestionElements;
		@SuppressWarnings("unused")
		public final double maximumWeight;
		
		public static SuggestHeader fromBytes(ByteBuffer buf) {
			int format = buf.getInt();
			int indexSize = buf.getInt();
			int suggestionElements = buf.getInt();
			double maximumWeight = buf.getDouble();
			
			return new SuggestHeader(format, indexSize, suggestionElements, maximumWeight);			
		}
		
		
	}

}
