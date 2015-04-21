/*
 * RTLCallReturn.java - This file is part of the Jakstab project.
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
package org.jakstab.rtl.statements;

import java.util.Collections;
import java.util.Set;

import org.jakstab.rtl.Context;
import org.jakstab.rtl.expressions.RTLMemoryLocation;
import org.jakstab.rtl.expressions.SetOfVariables;

public class RTLCallReturn extends AbstractRTLStatement {

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public RTLStatement evaluate(Context context) {
		return this;
	}

	@Override
	protected SetOfVariables initDefinedVariables() {
		return SetOfVariables.EMPTY_SET;
	}

	@Override
	protected SetOfVariables initUsedVariables() {
		return SetOfVariables.EMPTY_SET;
	}

	@Override
	protected Set<RTLMemoryLocation> initUsedMemoryLocations() {
		return Collections.emptySet();
	}

}
