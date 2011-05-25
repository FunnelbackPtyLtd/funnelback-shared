package com.funnelback.contentoptimiser.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.funnelback.contentoptimiser.HintMaxOther;
import com.funnelback.contentoptimiser.HintMaxPossible;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.Hint;


public class HintTest {

	@Test
	public void testHintMaxOther() {
		Hint h = new HintMaxOther("name");
		
		h.rememberScore(0.8f);
		h.rememberScore(0.6f);
		h.rememberScore(0.5f);
		h.rememberScore(0.4f);
		
		h.caculateWin(0.6f, 0.9f);
		
		assertTrue(h.isInteresting());
		assertEquals(0.2f,h.getWin(),0.00001f);
	}
	
	@Test
	public void testHintMaxOtherBoring() {
		
		// All scores the same, but we could do better
		Hint h = new HintMaxOther("name");
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.caculateWin(0.7f, 0.8f);
		assertFalse(h.isInteresting());
		
		// All scores the same except our lower score
		h = new HintMaxOther("name");
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.caculateWin(0.6f, 0.8f);
		assertTrue(h.isInteresting());

		// We're winning this feature, but we could be better
		h = new HintMaxOther("name");
		h.rememberScore(0.6f);
		h.rememberScore(0.6f);
		h.rememberScore(0.6f);
		h.rememberScore(0.6f);
		h.caculateWin(0.7f, 0.8f);
		assertFalse(h.isInteresting());
	}
	
	@Test
	public void testHintMaxPossibleBoring() {
		// All scores the same, but we could do better
		Hint h = new HintMaxPossible("name");
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.rememberScore(0.7f);
		h.caculateWin(0.7f, 0.8f);
		assertTrue(h.isInteresting());
		
		// We're winning this feature, but we could be better
		h = new HintMaxPossible("name");
		h.rememberScore(0.6f);
		h.rememberScore(0.6f);
		h.rememberScore(0.6f);
		h.rememberScore(0.6f);
		h.caculateWin(0.7f, 0.8f);
		assertTrue(h.isInteresting());

		// all scores the same, and minimum score
		h = new HintMaxPossible("name");
		h.rememberScore(0.0f);
		h.rememberScore(0.0f);
		h.rememberScore(0.0f);
		h.rememberScore(0.0f);
		h.caculateWin(0.0f, 0.8f);
		assertTrue(h.isInteresting());

		// all scores the same, and maximum score
		h = new HintMaxPossible("name");
		h.rememberScore(0.1f);
		h.rememberScore(0.1f);
		h.rememberScore(0.1f);
		h.rememberScore(0.1f);		
		h.caculateWin(0.1f, 0.1f);
		assertFalse(h.isInteresting());
	}

	
	
	
	@Test
	public void testHintMaxPossible() {
		Hint h = new HintMaxPossible("name");
		h.rememberScore(0.8f);
		h.rememberScore(0.6f);
		h.rememberScore(0.5f);
		h.rememberScore(0.4f);
		h.caculateWin(0.6f, 0.9f);
		
		assertTrue(h.isInteresting());
		assertEquals(0.3f,h.getWin(),0.00001f);
	}

}

