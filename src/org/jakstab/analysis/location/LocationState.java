/*
 * LocationState.java - This file is part of the Jakstab project.
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
package org.jakstab.analysis.location;

import java.util.Set;

import org.jakstab.analysis.*;
import org.jakstab.cfa.Location;
import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLNumber;
import org.jakstab.util.Logger;
import org.jakstab.util.Tuple;

/**
 * @author Johannes Kinder
 */
public class LocationState implements AbstractState, Comparable<LocationState> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LocationState.class);
	
	public static final LocationState TOP = new LocationState();
	public static final LocationState BOT = new LocationState();

	private final Location location;
	
	private LocationState() {
		location = null;
	}
	
	public LocationState(Location location) {
		assert location != null : "Cannot create control flow state with NULL location!";
		this.location = location;
	}
	
	@Override
	public String getIdentifier() {
		return location.toString();
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public LocationState join(LatticeElement l) {
		LocationState c = (LocationState)l;
		if (this.isBot()) return c;
		if (c.isBot() || this.equals(c)) return this;
		return TOP;
	}

	@Override
	public boolean isBot() {
		return this == BOT;
	}

	@Override
	public boolean isTop() {
		return this == TOP;
	}

	@Override
	public boolean lessOrEqual(LatticeElement l) {
		if (l.isTop() || this.equals(l)) return true;
		return false;
	}

	@Override
	public int hashCode() {
		if (isTop()) return 38941;
		if (isBot()) return 124767;
		return location.hashCode() + 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		LocationState other = (LocationState) obj;
		if (location == null) {
			// location is only null for singletons TOP and BOT  
			assert isTop() || isBot();
			return false;
		} 
		return location.equals(other.location);
	}
	
	@Override
	public String toString() {
		return location.toString();
	}

	@Override
	public Set<Tuple<RTLNumber>> projectionFromConcretization(
			RTLExpression... expressions) {
		return null;
	}

	@Override
	public int compareTo(LocationState o) {
		return location.compareTo(o.location);
	}

}
