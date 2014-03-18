/*
 * VpcLocation.java - This file is part of the Jakstab project.
 * Copyright 2007-2014 Johannes Kinder <jk@jakstab.org>
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

import org.jakstab.analysis.explicit.BasedNumberElement;
import org.jakstab.asm.AbsoluteAddress;

public class VpcLocation implements Location {
	private final BasedNumberElement vpc;
	private final RTLLabel location;
	
	public VpcLocation(BasedNumberElement vpc, RTLLabel location) {
		super();
		this.vpc = vpc;
		this.location = location;
	}
	
	public BasedNumberElement getVPC() {
		return vpc;
	}
	
	@Override
	public RTLLabel getLabel() {
		return location;
	}

	@Override
	public String toString() {
		return "vpc" + vpc + "_" + location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((vpc == null) ? 0 : vpc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VpcLocation other = (VpcLocation) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (vpc == null) {
			if (other.vpc != null)
				return false;
		} else if (!vpc.equals(other.vpc))
			return false;
		return true;
	}

	@Override
	public int compareTo(Location l) {
		if (!(l instanceof VpcLocation))
			throw new UnsupportedOperationException("Cannot compare RTLLabel to other location types.");
		VpcLocation other = (VpcLocation)l;
		if (vpc.equals(other.vpc))
			return location.compareTo(other.location);
		if (vpc.isTop())
			return 1;
		if (other.vpc.isTop())
			return -1;
		// Too lazy right now to implement something better
		return vpc.toString().compareTo(other.vpc.toString());
	}

	@Override
	public AbsoluteAddress getAddress() {
		return location.getAddress();
	}

}
