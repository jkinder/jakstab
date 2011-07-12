package org.jakstab.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jakstab.util.Logger;
import org.junit.Test;

public class SetsTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SetsTest.class);

	@Test
	public void testCrossProduct() {
		Set<Integer> set1 = new HashSet<Integer>(Arrays.asList(new Integer[]{1, 2, 3}));
		Set<Integer> set2 = new HashSet<Integer>(Arrays.asList(new Integer[]{5, 6, 7}));
		Tuple<Set<Integer>> tuple = new Tuple<Set<Integer>>(2);
		tuple.set(0, set1);
		tuple.set(1, set2);
		Set<Tuple<Integer>> result = Sets.crossProduct(tuple);
		Set<Tuple<Integer>> expected = new HashSet<Tuple<Integer>>();
		expected.add(Tuple.create(1, 5));
		expected.add(Tuple.create(2, 5));
		expected.add(Tuple.create(3, 5));
		expected.add(Tuple.create(1, 6));
		expected.add(Tuple.create(2, 6));
		expected.add(Tuple.create(3, 6));
		expected.add(Tuple.create(1, 7));
		expected.add(Tuple.create(2, 7));
		expected.add(Tuple.create(3, 7));
		
		assertEquals(expected, result);
	}

}
