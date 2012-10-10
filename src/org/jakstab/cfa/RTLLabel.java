/*
 * Location.java - This file is part of the Jakstab project.
 * Copyright 2007-2012 Johannes Kinder <jk@jakstab.org>
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
package org.jakstab.cfa;

import org.jakstab.asm.AbsoluteAddress;

/**
 * A location which uniquely identifies a RTLStatement in the program. It
 * consists of an address part, which equals the virtual address of the
 * instruction this statement is derived from, and of an index part
 * inside the instruction.
 * 
 * @author Johannes Kinder
 */
public class RTLLabel implements Location {
	private final AbsoluteAddress address;
	private final int index;
	
	public RTLLabel(AbsoluteAddress address) {
		this(address, 0);
	}

	public RTLLabel(AbsoluteAddress address, int index) {
		super();
		this.address = address;
		this.index = index;
	}
	
	public AbsoluteAddress getAddress() {
		return address;
	}
	
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return address.toString() + '_' + index;
	}

	@Override
	public int compareTo(Location l) {
		if (!(l instanceof RTLLabel))
			throw new UnsupportedOperationException("Cannot compare RTLLabel to other location types.");
		RTLLabel other = (RTLLabel)l;
		if (other.address.equals(address) && other.index == index) return 0;
		else if (other.address.getValue() < address.getValue() || 
				(other.address.getValue() == address.getValue()
						&& other.index < index)) return 1;
		else return -1;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + index;
		return result;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RTLLabel other = (RTLLabel) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public RTLLabel getLabel() {
		return this;
	}

}
