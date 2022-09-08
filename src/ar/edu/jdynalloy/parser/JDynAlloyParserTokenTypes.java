// $ANTLR 2.7.6 (2005-12-22): "jdynalloy.g" -> "JDynAlloyLexer.java"$

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
	
package ar.edu.jdynalloy.parser;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.*;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula.Operator;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntersection;
import ar.uba.dc.rfm.alloy.parser.ExpressionParser;
import ar.uba.dc.rfm.alloy.parser.FormalParametersDeclaration;
import ar.uba.dc.rfm.alloy.parser.IAlloyExpressionParseContext;	
import ar.edu.jdynalloy.parser.JDynAlloyParsingException;
import ar.edu.jdynalloy.parser.LabelUtils;

import ar.edu.jdynalloy.ast.*;

import ar.edu.jdynalloy.xlator.JType;

import ar.edu.jdynalloy.buffer.Represents;
import ar.edu.jdynalloy.buffer.JDynAlloyParserModuleBuffer;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.util.Collections;


public interface JDynAlloyParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LITERAL_module = 4;
	int IDENT = 5;
	int LITERAL_abstract = 6;
	int LITERAL_sig = 7;
	int LITERAL_extends = 8;
	int LITERAL_in = 9;
	int LBRACE = 10;
	int RBRACE = 11;
	int LITERAL_field = 12;
	int COLON = 13;
	int OBJECT_INVARIANT = 14;
	int CLASS_INVARIANT = 15;
	int OBJECT_CONSTRAINT = 16;
	int CLASS_CONSTRAINT = 17;
	int REPRESENTS = 18;
	int SUCH_THAT = 19;
	int VIRTUAL = 20;
	int PROGRAM = 21;
	int PURE = 22;
	int LBRACKET = 23;
	int COMMA = 24;
	int RBRACKET = 25;
	int SPECIFICATION = 26;
	int IMPLEMENTATION = 27;
	int SPECCASE = 28;
	int SPECCASE_ID = 29;
	int REQUIRES = 30;
	int ENSURES = 31;
	int MODIFIES = 32;
	int ASSERT = 33;
	int SEMICOLON = 34;
	int IF = 35;
	int ELSE = 36;
	int ASSUME = 37;
	int VAR = 38;
	int SKIP = 39;
	int CREATE_OBJECT = 40;
	int LT = 41;
	int GT = 42;
	int ASSIGNMENT = 43;
	int SUPER = 44;
	int CALL = 45;
	int WHILE = 46;
	int LOOP_INVARIANT = 47;
	int HAVOC = 48;
	int IMPLIES = 49;
	int OR = 50;
	int AND = 51;
	int LPAREN = 52;
	int RPAREN = 53;
	int NOT = 54;
	int LITERAL_some = 55;
	int LITERAL_all = 56;
	int LITERAL_lone = 57;
	int LITERAL_no = 58;
	int LITERAL_one = 59;
	int PIPE = 60;
	int STATICCALLSPEC = 61;
	int CALLSPEC = 62;
	int EQUALS = 63;
	int RARROW = 64;
	int PLUS = 65;
	int LITERAL_seq = 66;
	int LITERAL_set = 67;
	int SLASH = 68;
	int DOT = 69;
	int PLUSPLUS = 70;
	int AMPERSTAND = 71;
	int NUMBER = 72;
	int TRUE = 73;
	int FALSE = 74;
	int STAR = 75;
	int QUESTION = 76;
	int COMMENT = 77;
	int COMMENT_SLASH_SLASH = 78;
	int COMMENT_ML = 79;
	int WS = 80;
}
