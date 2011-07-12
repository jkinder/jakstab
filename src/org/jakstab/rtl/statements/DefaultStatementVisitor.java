/*
 * DefaultStatementVisitor.java - This file is part of the Jakstab project.
 * Copyright 2009-2011 Johannes Kinder <kinder@cs.tu-darmstadt.de>
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

import org.jakstab.util.Logger;

/**
 * Skeleton statement visitor that throws UnsupportedOperationException for every type
 * of statement. 
 * 
 * @author Johannes Kinder
 */
public abstract class DefaultStatementVisitor<T> implements StatementVisitor<T> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultStatementVisitor.class);

	private static final String errorMsg(RTLStatement stmt) {
		return "Visitor does not support statements of type " + 
		stmt.getClass().getSimpleName() + "!";
	}

	@Override
	public T visit(RTLVariableAssignment stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLMemoryAssignment stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLGoto stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLAssume stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLAssert stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLSkip stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLHalt stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLAlloc stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLDealloc stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLUnknownProcedureCall stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLHavoc stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}
	
	@Override
	public T visit(RTLMemset stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

	@Override
	public T visit(RTLMemcpy stmt) {
		throw new UnsupportedOperationException(errorMsg(stmt));
	}

}
