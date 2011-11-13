package com.funnelback.publicui.search.service;

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
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.PadreNative;
import com.funnelback.publicui.search.model.transaction.Suggestion;

@Component
public class DefaultSuggester implements Suggester {

	private static final int SIZEOF_HEADER = PadreNative.SizeOf.INT*3 + PadreNative.SizeOf.DOUBLE;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public List<Suggestion> suggest(String collectionId, String partialQuery,
			int numSuggestions, Sort sort, float alpha, AutoCMode autoCMode) {
		
		SortedSet<Suggestion> suggestions = new TreeSet<Suggestion>(new Suggestion.ByWeightComparator(.5f, partialQuery.length()));
		FileInputStream fis = null;
		
		try {
			Collection c = configRepository.getCollection(collectionId);
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
					Suggestion s = Suggestion.fromBytes(suggestionsbuf);
					char suggestionChar = s.suggestion.charAt(0);
					if (suggestionChar > queryChar) {
						reached = true;
						break;
					} else if ( suggestionChar == queryChar && s.suggestion.startsWith(partialQuery) && s.suggestion.length() > partialQuery.length()) {
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
	
	@RequiredArgsConstructor
	private static class SuggestHeader {
		public static final int FORMAT = 2;
		
		public final int format;
		public final int indexSize;
		public final int suggestionElements;
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
