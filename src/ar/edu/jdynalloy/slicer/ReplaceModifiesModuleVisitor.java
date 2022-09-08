/*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.edu.jdynalloy.slicer;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JCreateObject;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JHavoc;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JLoopInvariant;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JPostcondition;
import ar.edu.jdynalloy.ast.JPrecondition;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;
import ar.edu.jdynalloy.ast.JSkip;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.ast.JWhile;

class ReplaceModifiesModuleVisitor extends JDynAlloyVisitor {

	public ReplaceModifiesModuleVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		// TODO Auto-generated constructor stub
	}


	@Override
	public Object visit(JSpecCase node) {
		return node;
	}


	@Override
	public Object visit(JHavoc n) {
		return n;
	}

	@Override
	public Object visit(JAssert n) {
		return n;
	}

	@Override
	public Object visit(JAssignment n) {
		return n;
	}

	@Override
	public Object visit(JBlock n) {
		return n;
	}

	@Override
	public Object visit(JIfThenElse n) {
		return n;
	}

	@Override
	public Object visit(JSkip n) {
		return n;
	}

	@Override
	public Object visit(JWhile n) {
		return n;
	}

	@Override
	public Object visit(JCreateObject n) {
		return n;
	}

	@Override
	public Object visit(JVariableDeclaration n) {
		return n;
	}

	@Override
	public Object visit(JDynAlloyModule node) {
		return node;
	}

	@Override
	public Object visit(JProgramCall n) {
		return n;
	}

	@Override
	public Object visit(JProgramDeclaration node) {

		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		String qualifiedMethodName = node.getSignatureId() + "_" + node.getProgramId() + "_";
		if (methodToCheck.startsWith(qualifiedMethodName)) {

			// dynJAlloyBinding.
			return new JProgramDeclaration(node.isVirtual(), node.isConstructor(), node.isPure(), node.getSignatureId(), node.getProgramId(), 
					node.getParameters(), node.getSpecCases(), node.getBody(), null, null);
		} else {
			return node;
		}

	}

	@Override
	public Object visit(JSignature node) {
		return node;
	}

	@Override
	public Object visit(JField node) {
		return node;
	}

	@Override
	public Object visit(JAssume n) {
		return n;
	}

	@Override
	public Object visit(JLoopInvariant n) {
		return n;
	}

	@Override
	public Object visit(JObjectInvariant n) {
		return n;
	}

	@Override
	public Object visit(JObjectConstraint n) {
		return n;
	}

	@Override
	public Object visit(JRepresents n) {
		return n;
	}

	@Override
	public Object visit(JPostcondition n) {
		return n;
	}

	@Override
	public Object visit(JPrecondition n) {
		return n;
	}


}
