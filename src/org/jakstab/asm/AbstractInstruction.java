/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *  
 */

/* 
 * Original code for this class taken from the Java HotSpot VM. 
 * Modified for use with the Jakstab project. All modifications 
 * Copyright 2007-2015 Johannes Kinder <jk@jakstab.org>
 */

package org.jakstab.asm;

/**
 * Parent class for all instruction implementations providing default 
 * implementations for the {@link Instruction} interface. 
 */
public abstract class AbstractInstruction implements Instruction {
	protected final String name;

	/**
	 * Top level constructor for all instructions. Creates a new instruction given its menmonic.
	 * @param name the instruction mnemonic.
	 */
	public AbstractInstruction(String name) {
		if (name.contains(" "))//TODO-Dom Actually replace with real code this hack is worse than before
			name = name.split(" ")[1];
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString(long currentPc, SymbolFinder symFinder) {
		return name;
	}

}

