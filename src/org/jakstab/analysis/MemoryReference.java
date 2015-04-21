/*
 * MemoryReference.java - This file is part of the Jakstab project.
 * Copyright 2007-2015 Johannes Kinder <jk@jakstab.org>
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
package org.jakstab.analysis;

import org.jakstab.rtl.BitVectorType;

/**
 * A fixed reference to a memory location in a certain memory region.
 * 
 * @author Johannes Kinder
 */
public class MemoryReference implements ValueContainer, BitVectorType {

	private final MemoryRegion region;
	private final long offset;
	private final int bitwidth;
	
	public MemoryReference(MemoryRegion region, long offset, int bitwidth) {
		super();
		this.region = region;
		this.offset = offset;
		this.bitwidth = bitwidth;
	}

	public MemoryRegion getRegion() {
		return region;
	}

	public long getOffset() {
		return offset;
	}

	@Override
	public int getBitWidth() {
		return bitwidth;
	}

	@Override
	public String toString() {
		return "m" + bitwidth + "[" + region + " + " + offset + "]";
	}
}
