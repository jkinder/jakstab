/*
 * VpcPrecision.java - This file is part of the Jakstab project.
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
package org.jakstab.analysis.explicit;

import java.util.HashMap;
import java.util.Map;

import org.jakstab.analysis.Precision;
import org.jakstab.cfa.Location;

/**
 * Maps VPC values to explicit precision objects to realize the domain lifting for VPC-sensitivity.
 * 
 * @author Johannes Kinder
 */
public class VpcPrecision implements Precision {

	private Map<BasedNumberElement, ExplicitPrecision> vpcMap;
	private final Location location;
	
	VpcPrecision(Location location) {
		vpcMap = new HashMap<BasedNumberElement, ExplicitPrecision>();
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}

	public ExplicitPrecision getPrecision(BasedNumberElement vpcValue) {
		/*BasedNumberElement vpcValue;
		// If there's no VPC for this location (yet), assume TOP as VPC value 
		if (vpc == null) {
			vpcValue = BasedNumberElement.getTop(32);
		} else {
			vpcValue = b.getValue(vpc);
		}*/
		ExplicitPrecision eprec = vpcMap.get(vpcValue);
		if (eprec == null) {
			eprec = new ExplicitPrecision(BoundedAddressTracking.varThreshold.getValue());
			vpcMap.put(vpcValue, eprec);
		}
		return eprec;
	}
}
