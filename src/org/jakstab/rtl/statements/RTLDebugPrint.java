/*
 * RTLDebugPrint.java - This file is part of the Jakstab project.
 * Copyright 2009-2011 Johannes Kinder <kinder@cs.tu-darmstadt.de>
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
package org.jakstab.rtl.statements;

import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.util.Logger;

/**
 * Skip statement that causes Jakstab to print debug messages.
 * 
 * @author Johannes Kinder
 */
public class RTLDebugPrint extends RTLSkip {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RTLDebugPrint.class);
	
	private final String message;
	private final RTLExpression expression;
	
	public RTLDebugPrint(String message, RTLExpression expression) {
		this.message = message;
		this.expression = expression;
	}
	
	public String getMessage() {
		return message;
	}

	public RTLExpression getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return "DebugPrint(" + message + ", " + expression + ")";
	}
}
