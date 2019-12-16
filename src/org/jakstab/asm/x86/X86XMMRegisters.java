/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.jakstab.asm.x86;

import capstone.X86_const;
import org.jakstab.util.Logger;

/* There are 8 128-bit XMM registers*/

public class X86XMMRegisters {
	
	private final static Logger logger = Logger.getLogger(X86XMMRegisters.class);

	public static final int NUM_XMM_REGISTERS = 8;

	public static final X86XMMRegister XMM0;
	public static final X86XMMRegister XMM1;
	public static final X86XMMRegister XMM2;
	public static final X86XMMRegister XMM3;
	public static final X86XMMRegister XMM4;
	public static final X86XMMRegister XMM5;
	public static final X86XMMRegister XMM6;
	public static final X86XMMRegister XMM7;

	private static X86XMMRegister xmmRegisters[];

	static {
		//XMM registers
		XMM0 = new X86XMMRegister(X86_const.X86_REG_XMM0, "%xmm0");
		XMM1 = new X86XMMRegister(X86_const.X86_REG_XMM1, "%xmm1");
		XMM2 = new X86XMMRegister(X86_const.X86_REG_XMM2, "%xmm2");
		XMM3 = new X86XMMRegister(X86_const.X86_REG_XMM3, "%xmm3");
		XMM4 = new X86XMMRegister(X86_const.X86_REG_XMM4, "%xmm4");
		XMM5 = new X86XMMRegister(X86_const.X86_REG_XMM5, "%xmm5");
		XMM6 = new X86XMMRegister(X86_const.X86_REG_XMM6, "%xmm6");
		XMM7 = new X86XMMRegister(X86_const.X86_REG_XMM7, "%xmm7");

		xmmRegisters = (new X86XMMRegister[] {
				XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7
		});
	}

	public static int getNumberOfRegisters() {
		return NUM_XMM_REGISTERS;
	}

	//Return the register name
	public static String getRegisterName(int regNum) {
		if (regNum < 0 || regNum >= NUM_XMM_REGISTERS) {
			logger.error("Invalid XMM register number!");
			return null;
		}
		return xmmRegisters[regNum].toString();
	}

	public static X86XMMRegister getRegister(int regNum) {
		if (regNum < 0 || regNum >= NUM_XMM_REGISTERS) {
			logger.error("Invalid XMM register number!");
			return null;
		}
		return xmmRegisters[regNum];
	}
}
