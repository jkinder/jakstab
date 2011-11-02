/*
 * RTLOperationTest.java - This file is part of the Jakstab project.
 * Copyright 2009-2011 Johannes Kinder <jk@jakstab.org>
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, see <http://www.gnu.org/licenses/>.
 */
package org.jakstab.rtl.expressions;

import static org.junit.Assert.*;

import org.jakstab.rtl.*;
import org.jakstab.util.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Johannes Kinder
 */
public class RTLOperationTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(RTLOperationTest.class);

	private static ExpressionFactory factory;
	private static RTLNumber num1234_32;
	private static RTLNumber num7_32;
	private static RTLNumber num1_32;
	private static RTLNumber num0_32;
	private static RTLExpression opAnd;
	private static RTLNumber num5_8bit;
	private static RTLNumber num5_32bit;
	private static RTLNumber neg125_8;
	private static RTLVariable var8;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		factory = ExpressionFactory.getInstance();
		num1234_32 = factory.createNumber(1234, 32);
		num7_32 = factory.createNumber(7, 32);
		num1_32 = factory.createNumber(1, 32);
		num0_32 = factory.createNumber(0, 32);
		num5_8bit = factory.createNumber(5, 8);
		num5_32bit = factory.createNumber(5, 32);
		opAnd = factory.createAnd(num1234_32, num7_32); 
		var8 = factory.createVariable("y8", 8);
		neg125_8 = factory.createNumber(-125, 8);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.jakstab.rtl.expressions.RTLOperation#evaluate(org.jakstab.rtl.Context)}.
	 */
	@Test
	public void testEvaluate() throws TypeInferenceException {
		Context emptyContext = new Context();
		RTLExpression result = opAnd.evaluate(emptyContext);
		assertEquals(factory.createNumber(1234 & 7, 32), result);
		RTLExpression opShift = factory.createShiftRight(num1234_32, num1_32);
		result = opShift.evaluate(emptyContext);
		assertEquals(factory.createNumber(1234 >> 1, 32), result);
		
		assertEquals(factory.createNumber(1234 + 7 + 1, 32), factory.createPlus(num1234_32, num7_32, num1_32).evaluate(emptyContext));
		
		
		// removal of 32 bit int changes bitwidth of expression that 
		// includes a lower bit variable or number
		RTLExpression op = factory.createPlus(num0_32, factory.createSignExtend(factory.createNumber(8, 8), factory.createNumber(31, 8), var8));
		Context context = new Context();
		context.addAssignment(var8, num5_8bit);
		assertEquals(num5_32bit, op.evaluate(context));
		
		// Used to return !true... 
		op = factory.createEqual(factory.TRUE, factory.FALSE);
		result = op.evaluate(context);
		assertEquals(factory.FALSE, result);
	}
	
	@Test
	public void testBitOperations() {
		Context emptyContext = new Context();
		RTLExpression castedValue = factory.createZeroFill(
				factory.createNumber(8), 
				factory.createNumber(31), 
				num5_8bit);
		RTLExpression result = factory.createOr(num7_32, castedValue).evaluate(emptyContext);
		assertEquals(num7_32, result);
	}
	
	@Test
	public void testZeroFill() {
		Context emptyContext = new Context();
		RTLNumber m5 = factory.createNumber(-5, 8);
		RTLExpression result = factory.createZeroFill(8, 31, m5).evaluate(emptyContext);
		assertEquals(factory.createNumber(0xFB, 32), result);
	}
	
	@Test
	public void testSignExtend() {
		Context emptyContext = new Context();
		RTLNumber m5 = factory.createNumber(-5, 8);
		RTLExpression result = factory.createSignExtend(8, 31, m5).evaluate(emptyContext);
		assertEquals(32, result.getBitWidth());
		assertEquals(factory.createNumber(-5, 32), result);
	}
	
	@Test
	public void testMinus() throws TypeInferenceException {
		Context emptyContext = new Context();
		RTLExpression result = factory.createMinus(num1_32, num7_32).evaluate(emptyContext);
		assertEquals(factory.createNumber(-6, 32), result);
		
		result = factory.createMinus(num1234_32, num5_32bit).evaluate(emptyContext);
		assertEquals(factory.createNumber(1229, 32), result);
		
		result = factory.createMinus(neg125_8, num5_8bit).evaluate(emptyContext);
		assertEquals(factory.createNumber(126, 8), result);
		
		result = factory.createPlus(var8, num5_8bit);
		result = factory.createMinus(result, var8).evaluate(emptyContext);
		assertEquals(num5_8bit, result);

		result = factory.createPlus(factory.createNeg(num5_8bit), var8);
		result = factory.createMinus(var8, result).evaluate(emptyContext);
		assertEquals(num5_8bit, result);
}
	
	@Test
	public void testAdd() throws TypeInferenceException {
		Context emptyContext = new Context();
		RTLExpression result = factory.createPlus(factory.createNumber(125, 8), num5_8bit).evaluate(emptyContext);
		assertEquals(factory.createNumber(-126, 8), result);
		
		result = factory.createPlus(factory.createNumber(120, 8), 
				factory.createNumber(110, 8), 
				factory.createNumber(60, 8), 
				factory.createNumber(95, 8)).evaluate(emptyContext);
		assertEquals(factory.createNumber(-127, 8), result);
	}
	
	@Test
	public void testComparisons() {
		Context emptyContext = new Context();
		assertEquals(factory.TRUE, factory.createLessThan(num5_32bit, num1234_32).evaluate(emptyContext));
		assertEquals(factory.FALSE, factory.createLessThan(num5_8bit, neg125_8).evaluate(emptyContext));
		assertEquals(factory.TRUE, factory.createUnsignedLessThan(num5_8bit, neg125_8).evaluate(emptyContext));
		assertEquals(factory.TRUE, factory.createUnsignedLessOrEqual(num5_8bit, neg125_8).evaluate(emptyContext));
	}

}
