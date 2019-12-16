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

public enum DataType {

	INT8(8),
	UINT8(8),
	INT16(16),
	UINT16(16),
	INT32(32),
	UINT32(32),
	INT64(64),
	UINT64(64),
	INT128(128),
	UINT128(128),

	// float is 4 bytes, double is 8 bytes, extended double is 10 bytes
	// and quad is 16 bytes.

	FL_SINGLE(32),
	FL_DOUBLE(64),
	FL_EXT_DOUBLE(80),
	FL_QUAD(128),

	STRING(Integer.MIN_VALUE),

	UNKNOWN(Integer.MIN_VALUE);
	
	private int bits;
	
	DataType(int bits) {
		this.bits = bits;
	}

	public int bits() {
		return bits;
	}
	
	public static DataType intFromBits(int bitwidth, boolean signed) {
		return signed ? sIntfromBits(bitwidth) : uIntfromBits(bitwidth);
	}
	
	public static DataType sIntfromBits(int bitwidth) {
		switch (bitwidth) {
		case 8: return INT8;
		case 16: return INT16;
		case 32: return INT32;
		case 64: return INT64;
		case 128: return INT128;
		default: throw new IllegalArgumentException();
		}
	}

	public static DataType uIntfromBits(int bitwidth) {
		switch (bitwidth) {
		case 8: return UINT8;
		case 16: return UINT16;
		case 32: return UINT32;
		case 64: return UINT64;
		case 128: return UINT128;
		default: throw new IllegalArgumentException();
		}
	}
}
