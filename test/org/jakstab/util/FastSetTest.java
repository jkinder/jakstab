package org.jakstab.util;

import static org.junit.Assert.*;

import org.jakstab.util.Logger;
import org.junit.Test;

public class FastSetTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FastSetTest.class);

	@Test
	public void testAddContains() {
		FastSet<Integer> set = new FastSet<Integer>();
		set.add(5);
		assertTrue(set.contains(5));
		assertFalse(set.add(5));
		assertTrue(set.contains(5));
		assertFalse(set.contains(3));
		set.add(3);
		assertTrue(set.contains(5));
	}

}
