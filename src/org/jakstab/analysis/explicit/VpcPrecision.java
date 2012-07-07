package org.jakstab.analysis.explicit;

import java.util.HashMap;
import java.util.Map;

import org.jakstab.analysis.Precision;

public class VpcPrecision implements Precision {

	private Map<BasedNumberElement, ExplicitPrecision> vpcMap = new HashMap<BasedNumberElement, ExplicitPrecision>();
	
	public ExplicitPrecision getPrecision(BasedNumberElement vpc) {
		ExplicitPrecision eprec = vpcMap.get(vpc);
		if (eprec == null) {
			eprec = new ExplicitPrecision(BoundedAddressTracking.varThreshold.getValue());
			vpcMap.put(vpc, eprec);
		}
		return eprec;
	}
}
