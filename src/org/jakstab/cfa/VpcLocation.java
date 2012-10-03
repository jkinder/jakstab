package org.jakstab.cfa;

import org.jakstab.analysis.explicit.BasedNumberElement;

public 	class VpcLocation {
	private final BasedNumberElement vpc;
	private final Location location;
	
	public VpcLocation(BasedNumberElement vpc, Location location) {
		super();
		this.vpc = vpc;
		this.location = location;
	}
	
	public BasedNumberElement getVPC() {
		return vpc;
	}
	
	public Location getLocation() {
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
	
}
