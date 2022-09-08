// $ANTLR 2.7.6 (2005-12-22): "jdynalloy.g" -> "JDynAlloyParser.java"$

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


import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class JDynAlloyParser extends antlr.LLkParser       implements JDynAlloyParserTokenTypes
 {
	
	
		JDynAlloyParserModuleBuffer buffer;
	
		//private AlloyTyping fields = new AlloyTyping();
		//private JDynAlloyProgramParseContext classCtx = new JDynAlloyProgramParseContext(Collections.<AlloyVariable>emptySet(),Collections.<AlloyVariable>emptySet(),true);
		List<JDynAlloyModule> modules = new ArrayList<JDynAlloyModule>();
		
	public static AlloyExpression buildTerm(IAlloyExpressionParseContext ctx, Token t, Token aliasModuleIdToken) {
		String text = t.getText();
		if (ctx.isIntLiteral(text))
		    return new ExprIntLiteral(ctx.getIntLiteral(text));
		if (ctx.isVariableName(text))
			return new ExprVariable(ctx.getAlloyVariable(text));
		else
			return new ExprConstant(aliasModuleIdToken==null? null: aliasModuleIdToken.getText(), text); 
	}
	
	
	public static void extendAlloyTyping(FormalParametersDeclaration at, List<String> vars, String t) {
		for (String var : vars) {
			at.put(new AlloyVariable(var), t); 
		}
	}
	
	public static String join(String s1, String s2)
	{
		if ( s1 == null || s1.length() == 0 )
			return s2;
		else if ( s2 == null || s2.length() == 0 )
			return s1;
		else
			return s1 + " " + s2;
	}
	
	public static String relationType(String type1, String mult1, String mult2, String type2)
	{
		return ExpressionParser.join(type1, mult1) + " -> " + ExpressionParser.join(mult2, type2); 
	}		
		
	private AlloyFormula buildImpliesFormula(AlloyFormula... fs) {
		AlloyFormula result = null;
		for (int i = 0; i < fs.length; i++) {
			AlloyFormula f = fs[i];
			if (result == null)
				result = f;
			else
				result = new ImpliesFormula(result, f);
		}
		return result;
	}		
				
	private JDynAlloyProgramParseContext globalCtx = new JDynAlloyProgramParseContext(Collections.<AlloyVariable>singletonList(AlloyVariable.buildAlloyVariable("thiz")),Collections.<AlloyVariable>emptySet(),true);
	
	public void setGlobalCtx(JDynAlloyProgramParseContext ctx) {
		globalCtx = ctx;
	}
	
		

protected JDynAlloyParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public JDynAlloyParser(TokenBuffer tokenBuf) {
  this(tokenBuf,7);
}

protected JDynAlloyParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public JDynAlloyParser(TokenStream lexer) {
  this(lexer,7);
}

public JDynAlloyParser(ParserSharedInputState state) {
  super(state,7);
  tokenNames = _tokenNames;
}

	public final List<JDynAlloyModule>  dynJmlAlloyModules() throws RecognitionException, TokenStreamException {
		List<JDynAlloyModule> r;
		
		
				r = new ArrayList<JDynAlloyModule>();
				JDynAlloyModule dynJmlAlloyModule1;
				JDynAlloyProgramParseContext ctx = new JDynAlloyProgramParseContext(globalCtx);
				
				   
			
		
		{
		int _cnt3=0;
		_loop3:
		do {
			if ((LA(1)==LITERAL_module)) {
				dynJmlAlloyModule1=dynJmlAlloyModule(ctx);
				r.add(dynJmlAlloyModule1);
			}
			else {
				if ( _cnt3>=1 ) { break _loop3; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt3++;
		} while (true);
		}
		return r;
	}
	
	public final JDynAlloyModule  dynJmlAlloyModule(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JDynAlloyModule r;
		
		Token  moduleId = null;
		Token  alloySignature = null;
		Token  superClassName = null;
		Token  inSuperSet = null;
		
				
					buffer = new JDynAlloyParserModuleBuffer();
					JField field;
					List<JField> fields = new ArrayList<JField>();
					JObjectInvariant objectInvariant;
					JClassInvariant classInvariant;
					JClassConstraint classConstraint;
					JObjectConstraint objectConstraint;
					
					JRepresents represents;
					JProgramDeclaration programDeclaration;
					
					ctx.addAlloyVariable(AlloyVariable.buildAlloyVariable("thiz"));
					
					AlloyFormula alloyFormula1 = null;
		
		
		match(LITERAL_module);
		moduleId = LT(1);
		match(IDENT);
		{
		switch ( LA(1)) {
		case LITERAL_abstract:
		{
			match(LITERAL_abstract);
			buffer.setAbstract(true);
			break;
		}
		case LITERAL_sig:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(LITERAL_sig);
		alloySignature = LT(1);
		match(IDENT);
		{
		if (((LA(1) >= LITERAL_extends && LA(1) <= LBRACE)) && (LA(2)==IDENT||LA(2)==RBRACE) && (LA(3)==LBRACE)) {
			{
			switch ( LA(1)) {
			case LITERAL_extends:
			{
				match(LITERAL_extends);
				superClassName = LT(1);
				match(IDENT);
				buffer.setSuperClassSignatureId(superClassName.getText());
				break;
			}
			case LITERAL_in:
			{
				match(LITERAL_in);
				inSuperSet = LT(1);
				match(IDENT);
				buffer.setInSignatureId(inSuperSet.getText());
				break;
			}
			case LBRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LBRACE);
			match(RBRACE);
		}
		else if ((LA(1)==LBRACE) && (_tokenSet_0.member(LA(2))) && (_tokenSet_1.member(LA(3)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(LBRACE);
		{
		switch ( LA(1)) {
		case IDENT:
		case SUPER:
		case LPAREN:
		case NOT:
		case LITERAL_some:
		case LITERAL_all:
		case LITERAL_lone:
		case LITERAL_no:
		case LITERAL_one:
		case STATICCALLSPEC:
		case CALLSPEC:
		case NUMBER:
		case TRUE:
		case FALSE:
		{
			alloyFormula1=alloyFormula(ctx);
			buffer.setFact(alloyFormula1);
			break;
		}
		case RBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RBRACE);
		{
		_loop10:
		do {
			if ((LA(1)==LITERAL_field)) {
				field=jField(ctx);
				buffer.getFields().put(field.getFieldVariable(), field.getFieldType());
			}
			else {
				break _loop10;
			}
			
		} while (true);
		}
		{
		_loop12:
		do {
			switch ( LA(1)) {
			case OBJECT_INVARIANT:
			{
				objectInvariant=jObjectInvariant(ctx);
				buffer.getInvariants().add(objectInvariant.getFormula());
				break;
			}
			case CLASS_INVARIANT:
			{
				classInvariant=jClassInvariant(ctx);
				buffer.getStaticInvariants().add(classInvariant.getFormula());
				break;
			}
			case OBJECT_CONSTRAINT:
			{
				objectConstraint=jObjectConstraint(ctx);
				buffer.getConstraints().add(objectConstraint.getFormula());
				break;
			}
			case CLASS_CONSTRAINT:
			{
				classConstraint=jClassConstraint(ctx);
				buffer.getStaticConstraints().add(classConstraint.getFormula());
				break;
			}
			case REPRESENTS:
			{
				represents=jRepresents(ctx);
				buffer.getRepresents().add(new Represents(represents.getExpression(), represents.getExpressionType(),represents.getFormula()));
				break;
			}
			case VIRTUAL:
			case PROGRAM:
			{
					JDynAlloyProgramParseContext programCtx = new JDynAlloyProgramParseContext(ctx);
				programDeclaration=jProgramDeclaration(programCtx);
				buffer.getPrograms().add(programDeclaration);
				break;
			}
			default:
			{
				break _loop12;
			}
			}
		} while (true);
		}
		
				if (!moduleId.getText().equals(alloySignature.getText())) {
					throw new RuntimeException("Module name must equal Signature name"); 
				}
				
				buffer.setSignatureId(alloySignature.getText());
				buffer.setThisType(new JType(alloySignature.getText()));
				r = buffer.getModule();
		
			
		return r;
	}
	
	public final AlloyFormula  alloyFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyFormula r;
		
		
		r=impliesFormula(ctx);
		return r;
	}
	
	public final JField  jField(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JField r;
		
		Token  fieldName = null;
		
			JType jType;
		
		
		match(LITERAL_field);
		fieldName = LT(1);
		match(IDENT);
		match(COLON);
		jType=alloyType();
		match(LBRACE);
		match(RBRACE);
		
			AlloyVariable alloyVariable = AlloyVariable.buildAlloyVariable(fieldName.getText());
			r = new JField(alloyVariable, jType);
			ctx.addAlloyField(alloyVariable);	 
		
		return r;
	}
	
	public final JObjectInvariant  jObjectInvariant(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JObjectInvariant r;
		
		
			AlloyFormula alloyFormula1;
		
		
		match(OBJECT_INVARIANT);
		alloyFormula1=alloyFormula(ctx);
		r = new JObjectInvariant(alloyFormula1);
		return r;
	}
	
	public final JClassInvariant  jClassInvariant(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JClassInvariant r;
		
		
		AlloyFormula alloyFormula1;
		
		
		match(CLASS_INVARIANT);
		alloyFormula1=alloyFormula(ctx);
		r = new JClassInvariant(alloyFormula1);
		return r;
	}
	
	public final JObjectConstraint  jObjectConstraint(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JObjectConstraint r;
		
		
		AlloyFormula alloyFormula1;
		
		
		match(OBJECT_CONSTRAINT);
		alloyFormula1=alloyFormula(ctx);
		r = new JObjectConstraint(alloyFormula1);
		return r;
	}
	
	public final JClassConstraint  jClassConstraint(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JClassConstraint r;
		
		
		AlloyFormula alloyFormula1;
		
		
		match(CLASS_CONSTRAINT);
		alloyFormula1=alloyFormula(ctx);
		r = new JClassConstraint(alloyFormula1);
		return r;
	}
	
	public final JRepresents  jRepresents(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JRepresents r;
		
		
			AlloyFormula alloyFormula1;
			AlloyExpression expression1;
		
		
		match(REPRESENTS);
		expression1=termExpression(ctx);
		match(SUCH_THAT);
		alloyFormula1=alloyFormula(ctx);
		
				AlloyVariable fieldName;
				
				if (expression1 instanceof ExprJoin ) {
					ExprJoin exprJoin = (ExprJoin) expression1; 
					if (!(exprJoin.getLeft() instanceof ExprVariable && ((ExprVariable) exprJoin.getLeft()).getVariable().getVariableId().getString().equals("thiz") )) {
						throw new JDynAlloyParsingException("Invalid 'Represents Field' expression. Only join expression are supported with 'thiz' in left expression");								
					}
					if (!(exprJoin.getRight() instanceof ExprVariable) ) {
						throw new JDynAlloyParsingException("Invalid 'Represents Field' expression. Only join expression are supported with field name in right expression");
					}
					ExprVariable exprVariable = (ExprVariable) exprJoin.getRight();
					fieldName = exprVariable.getVariable();  
				} else if (expression1 instanceof ExprVariable) {
					ExprVariable exprVariable = (ExprVariable) expression1;
					fieldName = exprVariable.getVariable();
				} else {
					throw new JDynAlloyParsingException("Invalid 'Represents Field' expression");
				}
				
				JType representsFieldType = buffer.getFields().getJAlloyType(fieldName);
				if (representsFieldType == null) {
					throw new JDynAlloyParsingException("Represent field " + fieldName + " has reference to field don't declared (field must be declared prior to represents fields");
				}
			
				r = new JRepresents(expression1, representsFieldType, alloyFormula1);
		
		
		return r;
	}
	
	public final JProgramDeclaration  jProgramDeclaration(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JProgramDeclaration r;
		
		Token  moduleName = null;
		Token  programName = null;
		
			boolean isAbstract = false;
			boolean isConstructor = false;
			boolean isPure = false;
			String programId;
			String signatureId;
			List<JSpecCase> specCases = new ArrayList<JSpecCase>();
			List<JVariableDeclaration> parameters = new ArrayList<JVariableDeclaration>();
			JStatement body;
			
			JVariableDeclaration variableDeclaration1;
			JSpecCase specCase1;
		
		
		
		{
		switch ( LA(1)) {
		case VIRTUAL:
		{
			match(VIRTUAL);
			isAbstract = true;
			break;
		}
		case PROGRAM:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(PROGRAM);
		{
		switch ( LA(1)) {
		case PURE:
		{
			match(PURE);
			isPure = true;
			break;
		}
		case IDENT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		moduleName = LT(1);
		match(IDENT);
		match(COLON);
		match(COLON);
		programName = LT(1);
		match(IDENT);
		match(LBRACKET);
		variableDeclaration1=jVariableDeclaration(ctx);
		parameters.add(variableDeclaration1);
		{
		_loop23:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				variableDeclaration1=jVariableDeclaration(ctx);
				parameters.add(variableDeclaration1);
			}
			else {
				break _loop23;
			}
			
		} while (true);
		}
		match(RBRACKET);
		{
		switch ( LA(1)) {
		case SPECIFICATION:
		{
			match(SPECIFICATION);
			match(LBRACE);
			{
			_loop26:
			do {
				if ((LA(1)==SPECCASE)) {
					specCase1=jSpecCase(ctx);
					specCases.add(specCase1);
				}
				else {
					break _loop26;
				}
				
			} while (true);
			}
			match(RBRACE);
			break;
		}
		case IMPLEMENTATION:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(IMPLEMENTATION);
		body=jBlock(ctx);
		
			signatureId = moduleName.getText();
			programId = programName.getText();
			isConstructor = programName.getText().equals("Constructor");
			r = new JProgramDeclaration(isAbstract, isConstructor, isPure, signatureId, programId, parameters, specCases, body, new AlloyTyping(), new ArrayList<AlloyFormula>());
		
		return r;
	}
	
	public final JType  alloyType() throws RecognitionException, TokenStreamException {
		JType r;
		
		
			String s = null;
		
		
		s=typeName();
		r = JType.parse(s);
		return r;
	}
	
	public final AlloyExpression  termExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		r=joinExpression(ctx);
		{
		if ((LA(1)==PLUS)) {
			match(PLUS);
			other=termExpression(ctx);
			r = new ExprUnion(r, other);
		}
		else if ((LA(1)==EOF) && (_tokenSet_2.member(LA(2))) && (_tokenSet_3.member(LA(3))) && (_tokenSet_4.member(LA(4))) && (_tokenSet_5.member(LA(5))) && (_tokenSet_6.member(LA(6))) && (_tokenSet_7.member(LA(7)))) {
			match(Token.EOF_TYPE);
		}
		else if ((_tokenSet_2.member(LA(1))) && (_tokenSet_3.member(LA(2))) && (_tokenSet_4.member(LA(3))) && (_tokenSet_5.member(LA(4))) && (_tokenSet_6.member(LA(5))) && (_tokenSet_7.member(LA(6))) && (_tokenSet_7.member(LA(7)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return r;
	}
	
	public final JVariableDeclaration  jVariableDeclaration(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JVariableDeclaration r;
		
		Token  id = null;
		JType alloyType;
		
		match(VAR);
		id = LT(1);
		match(IDENT);
		match(COLON);
		alloyType=alloyType();
		
			AlloyVariable variable = AlloyVariable.buildAlloyVariable(id.getText());
			r = new JVariableDeclaration(variable,alloyType);
			ctx.addAlloyVariable(variable);	 
		
		return r;
	}
	
	public final JSpecCase  jSpecCase(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JSpecCase r;
		
		
			JPrecondition precondition1;
			JPostcondition postcondition1; 
			JModifies modifies1;
			
			List<JPrecondition> requires = new ArrayList<JPrecondition>();
			List<JPostcondition> ensures = new ArrayList<JPostcondition>();
			List<JModifies> modifies = new ArrayList<JModifies>();
		
		
		match(SPECCASE);
		match(SPECCASE_ID);
		match(LBRACE);
		{
		_loop29:
		do {
			switch ( LA(1)) {
			case REQUIRES:
			{
				precondition1=jRequires(ctx);
				requires.add(precondition1);
				break;
			}
			case ENSURES:
			{
				postcondition1=jEnsures(ctx);
				ensures.add(postcondition1);
				break;
			}
			case MODIFIES:
			{
				modifies1=jModifies(ctx);
				modifies.add(modifies1);
				break;
			}
			default:
			{
				break _loop29;
			}
			}
		} while (true);
		}
		match(RBRACE);
		
			r=new JSpecCase(requires, ensures, modifies);
		
		return r;
	}
	
	public final JBlock  jBlock(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JBlock r;
		
		
			List<JStatement> statementList = new ArrayList<JStatement>();
			JStatement aStatement;
		
		
		match(LBRACE);
		{
		int _cnt42=0;
		_loop42:
		do {
			if ((_tokenSet_8.member(LA(1)))) {
				aStatement=jStatement(ctx);
				statementList.add(aStatement);
			}
			else {
				if ( _cnt42>=1 ) { break _loop42; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt42++;
		} while (true);
		}
		match(RBRACE);
		r = new JBlock(statementList.toArray(new JStatement[0]));
		return r;
	}
	
	public final JPrecondition  jRequires(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JPrecondition r;
		
		
			AlloyFormula alloyFormula1;
		
		
		match(REQUIRES);
		match(LBRACE);
		alloyFormula1=alloyFormula(ctx);
		match(RBRACE);
		r=new JPrecondition(alloyFormula1);
		return r;
	}
	
	public final JPostcondition  jEnsures(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JPostcondition r;
		
		
			AlloyFormula alloyFormula1;
		
		
		match(ENSURES);
		match(LBRACE);
		alloyFormula1=alloyFormula(ctx);
		match(RBRACE);
		r=new JPostcondition(alloyFormula1);
		return r;
	}
	
	public final JModifies  jModifies(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JModifies r;
		
		
			AlloyExpression alloyExpression1;
		
		
		match(MODIFIES);
		match(LBRACE);
		alloyExpression1=termExpression(ctx);
		match(RBRACE);
		r=new JModifies(alloyExpression1);
		return r;
	}
	
	public final JStatement  jStatement(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JStatement r;
		
		
		switch ( LA(1)) {
		case ASSERT:
		{
			r=jAssert(ctx);
			break;
		}
		case ASSUME:
		{
			r=jAssume(ctx);
			break;
		}
		case VAR:
		{
			r=jVariableDeclarationStatement(ctx);
			break;
		}
		case SKIP:
		{
			r=jSkip(ctx);
			break;
		}
		case IF:
		{
			r=jIfThenElse(ctx);
			break;
		}
		case CREATE_OBJECT:
		{
			r=jCreateObject(ctx);
			break;
		}
		case IDENT:
		case LPAREN:
		case NUMBER:
		case TRUE:
		case FALSE:
		{
			r=jAssignment(ctx);
			break;
		}
		case SUPER:
		case CALL:
		{
			r=jProgramCall(ctx);
			break;
		}
		case WHILE:
		{
			r=jWhile(ctx);
			break;
		}
		case LBRACE:
		{
			r=jBlock(ctx);
			break;
		}
		case HAVOC:
		{
			r=jHavoc(ctx);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return r;
	}
	
	public final JAssert  jAssert(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JAssert r;
		
		AlloyFormula alloyFormula1;
		
		match(ASSERT);
		alloyFormula1=alloyFormula(ctx);
		match(SEMICOLON);
		r = new JAssert(alloyFormula1);
		return r;
	}
	
	public final JAssume  jAssume(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JAssume r;
		
		AlloyFormula alloyFormula1;
		
		match(ASSUME);
		alloyFormula1=alloyFormula(ctx);
		match(SEMICOLON);
		r = new JAssume(alloyFormula1);
		return r;
	}
	
	public final JVariableDeclaration  jVariableDeclarationStatement(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JVariableDeclaration r;
		
		
		r=jVariableDeclaration(ctx);
		match(SEMICOLON);
		return r;
	}
	
	public final JSkip  jSkip(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JSkip r;
		
		AlloyFormula alloyFormula1;
		
		match(SKIP);
		match(SEMICOLON);
		r = new JSkip();
		return r;
	}
	
	public final JIfThenElse  jIfThenElse(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JIfThenElse r;
		
		
			AlloyFormula alloyFormula1;
			JStatement thenStatement;
			JStatement elseStatement;	
		
		
		match(IF);
		alloyFormula1=alloyFormula(ctx);
		thenStatement=jBlock(ctx);
		match(ELSE);
		elseStatement=jBlock(ctx);
		match(SEMICOLON);
		
			
			r = new JIfThenElse(alloyFormula1,thenStatement,elseStatement,LabelUtils.nextIfLabel());
		
		return r;
	}
	
	public final JCreateObject  jCreateObject(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JCreateObject r;
		
		Token  className = null;
		Token  variableName = null;
		
		match(CREATE_OBJECT);
		match(LT);
		className = LT(1);
		match(IDENT);
		match(GT);
		match(LBRACKET);
		variableName = LT(1);
		match(IDENT);
		match(RBRACKET);
		match(SEMICOLON);
		r= new JCreateObject(className.getText(), AlloyVariable.buildAlloyVariable(variableName.getText()));
		return r;
	}
	
	public final JAssignment  jAssignment(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JAssignment r;
		
		
			AlloyExpression left;
			AlloyExpression right;
		
		
		left=termExpression(ctx);
		match(ASSIGNMENT);
		right=termExpression(ctx);
		match(SEMICOLON);
		r = new JAssignment(left, right);
		return r;
	}
	
	public final JProgramCall  jProgramCall(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JProgramCall r;
		
		Token  programName = null;
		
			AlloyExpression parameter;
			List<AlloyExpression> parameterList = new ArrayList<AlloyExpression>();
			boolean isSuper = false;
		
		
		{
		switch ( LA(1)) {
		case SUPER:
		{
			match(SUPER);
			isSuper = true;
			break;
		}
		case CALL:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(CALL);
		programName = LT(1);
		match(IDENT);
		match(LBRACKET);
		parameter=termExpression(ctx);
		parameterList.add(parameter);
		{
		_loop48:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				parameter=termExpression(ctx);
				parameterList.add(parameter);
			}
			else {
				break _loop48;
			}
			
		} while (true);
		}
		match(RBRACKET);
		match(SEMICOLON);
		r = new JProgramCall(isSuper,programName.getText(),parameterList);
		return r;
	}
	
	public final JWhile  jWhile(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JWhile r;
		
		
			AlloyFormula condition;
			JStatement body;
			AlloyFormula loopInvariantFormula = null;
			
		
		
		match(WHILE);
		condition=alloyFormula(ctx);
		{
		switch ( LA(1)) {
		case LOOP_INVARIANT:
		{
			match(LOOP_INVARIANT);
			loopInvariantFormula=alloyFormula(ctx);
			break;
		}
		case LBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		body=jBlock(ctx);
		match(SEMICOLON);
		
			JLoopInvariant loopInvariant;
			if (loopInvariantFormula == null) {
				loopInvariant = null;
			} else {
				loopInvariant = new JLoopInvariant(loopInvariantFormula);
			}
			r = new JWhile(condition,body,loopInvariant,LabelUtils.nextWhileLabel());	
		
		return r;
	}
	
	public final JHavoc  jHavoc(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		JHavoc r;
		
		
			AlloyExpression expression1;
		
		
		match(HAVOC);
		expression1=termExpression(ctx);
		match(SEMICOLON);
		
			r = new JHavoc( expression1 );
		
		return r;
	}
	
	public final String  typeName() throws RecognitionException, TokenStreamException {
		String r;
		
		Token  id = null;
		r = ""; String m=null;
		
		{
		int _cnt84=0;
		_loop84:
		do {
			switch ( LA(1)) {
			case LITERAL_some:
			case LITERAL_lone:
			case LITERAL_one:
			case LITERAL_seq:
			case LITERAL_set:
			{
				m=multiplicity();
				r=r+= " " + m + " ";
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				r=r+="(";
				break;
			}
			case RPAREN:
			{
				match(RPAREN);
				r=r+=")";
				break;
			}
			case RARROW:
			{
				match(RARROW);
				r=r+="->";
				break;
			}
			case PLUS:
			{
				match(PLUS);
				r=r+="+";
				break;
			}
			default:
				if ((LA(1)==IDENT) && (_tokenSet_9.member(LA(2))) && (_tokenSet_10.member(LA(3))) && (_tokenSet_11.member(LA(4))) && (_tokenSet_12.member(LA(5))) && (_tokenSet_13.member(LA(6))) && (_tokenSet_14.member(LA(7)))) {
					id = LT(1);
					match(IDENT);
					r=r+= id.getText();
				}
			else {
				if ( _cnt84>=1 ) { break _loop84; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			}
			_cnt84++;
		} while (true);
		}
		
			
		return r;
	}
	
	public final AlloyFormula  impliesFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyFormula r;
		
		
			AlloyFormula right = null;
			List<AlloyFormula> rightList = new ArrayList<AlloyFormula>();
		
		
		r=orFormula(ctx);
		rightList.add(r);
		{
		_loop56:
		do {
			if ((LA(1)==IMPLIES) && (_tokenSet_15.member(LA(2))) && (_tokenSet_16.member(LA(3))) && (_tokenSet_17.member(LA(4))) && (_tokenSet_18.member(LA(5))) && (_tokenSet_19.member(LA(6))) && (_tokenSet_20.member(LA(7)))) {
				match(IMPLIES);
				right=orFormula(ctx);
				rightList.add(right);
			}
			else {
				break _loop56;
			}
			
		} while (true);
		}
		
				if ( rightList.size() > 1 ) {
					r = this.buildImpliesFormula(rightList.toArray(new AlloyFormula[0]));
				} 		
			
		return r;
	}
	
	public final AlloyFormula  orFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyFormula r;
		
		
			AlloyFormula right; 
			List<AlloyFormula> rightList = new ArrayList<AlloyFormula>();
		
		
		r=andFormula(ctx);
		rightList.add(r);
		{
		_loop59:
		do {
			if ((LA(1)==OR) && (_tokenSet_15.member(LA(2))) && (_tokenSet_16.member(LA(3))) && (_tokenSet_17.member(LA(4))) && (_tokenSet_18.member(LA(5))) && (_tokenSet_19.member(LA(6))) && (_tokenSet_20.member(LA(7)))) {
				match(OR);
				right=andFormula(ctx);
				rightList.add(right);
			}
			else {
				break _loop59;
			}
			
		} while (true);
		}
		
				if ( rightList.size() > 1 ) {
					r = OrFormula.buildOrFormula(rightList.toArray(new AlloyFormula[0]));
				} 
			
		return r;
	}
	
	public final AlloyFormula  andFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyFormula r;
		
		
			AlloyFormula right; 
			List<AlloyFormula> rightList = new ArrayList<AlloyFormula>();
		
		
		r=atomFormula(ctx);
		rightList.add(r);
		{
		_loop62:
		do {
			if ((LA(1)==AND) && (_tokenSet_15.member(LA(2))) && (_tokenSet_16.member(LA(3))) && (_tokenSet_17.member(LA(4))) && (_tokenSet_18.member(LA(5))) && (_tokenSet_19.member(LA(6))) && (_tokenSet_20.member(LA(7)))) {
				match(AND);
				right=atomFormula(ctx);
				rightList.add(right);
			}
			else {
				break _loop62;
			}
			
		} while (true);
		}
		
				if ( rightList.size() > 1 ) {
					r = AndFormula.buildAndFormula(rightList.toArray(new AlloyFormula[0]));
				} 
			
		return r;
	}
	
	public final AlloyFormula  atomFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyFormula r;
		
		
		switch ( LA(1)) {
		case SUPER:
		case STATICCALLSPEC:
		case CALLSPEC:
		{
			r=callSpecFormula(ctx);
			break;
		}
		case LITERAL_some:
		case LITERAL_all:
		case LITERAL_lone:
		case LITERAL_no:
		case LITERAL_one:
		{
			r=quantifiedFormula(ctx);
			break;
		}
		case NOT:
		{
			match(NOT);
			r=alloyFormula(ctx);
			r = new NotFormula(r);
			break;
		}
		default:
			if ((LA(1)==IDENT) && (LA(2)==LBRACKET||LA(2)==SLASH) && (_tokenSet_21.member(LA(3))) && (_tokenSet_22.member(LA(4))) && (_tokenSet_23.member(LA(5))) && (_tokenSet_4.member(LA(6))) && (_tokenSet_5.member(LA(7)))) {
				r=predicateFormula(ctx);
			}
			else if ((LA(1)==LPAREN) && (_tokenSet_15.member(LA(2))) && (_tokenSet_16.member(LA(3))) && (_tokenSet_17.member(LA(4))) && (_tokenSet_24.member(LA(5))) && (_tokenSet_25.member(LA(6))) && (_tokenSet_19.member(LA(7)))) {
				match(LPAREN);
				r=alloyFormula(ctx);
				match(RPAREN);
			}
			else if ((_tokenSet_26.member(LA(1))) && (_tokenSet_27.member(LA(2))) && (_tokenSet_28.member(LA(3))) && (_tokenSet_29.member(LA(4))) && (_tokenSet_30.member(LA(5))) && (_tokenSet_4.member(LA(6))) && (_tokenSet_5.member(LA(7)))) {
				r=equalsFormula(ctx);
			}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return r;
	}
	
	public final PredicateFormula  predicateFormula(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		PredicateFormula r;
		
		Token  aliasModuleId = null;
		Token  pred = null;
		r = null; List<AlloyExpression> args = null;
		
		{
		if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
			aliasModuleId = LT(1);
			match(IDENT);
			match(SLASH);
		}
		else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		pred = LT(1);
		match(IDENT);
		match(LBRACKET);
		args=actualParams(ctx);
		match(RBRACKET);
		r = new PredicateFormula(aliasModuleId==null?null:aliasModuleId.getText(),pred.getText(), args);
		return r;
	}
	
	public final PredicateCallAlloyFormula   callSpecFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		PredicateCallAlloyFormula  r;
		
		Token  programName = null;
		
			AlloyExpression parameter;
			List<AlloyExpression> parameterList = new ArrayList<AlloyExpression>();
			boolean isSuper = false;
			boolean isStatic = false;
		
		
		{
		switch ( LA(1)) {
		case SUPER:
		{
			match(SUPER);
			isSuper = true;
			break;
		}
		case STATICCALLSPEC:
		case CALLSPEC:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case STATICCALLSPEC:
		{
			match(STATICCALLSPEC);
			isStatic = true;
			break;
		}
		case CALLSPEC:
		{
			match(CALLSPEC);
			isStatic = false;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		programName = LT(1);
		match(IDENT);
		match(LBRACKET);
		parameter=termExpression(ctx);
		parameterList.add(parameter);
		{
		_loop72:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				parameter=termExpression(ctx);
				parameterList.add(parameter);
			}
			else {
				break _loop72;
			}
			
		} while (true);
		}
		match(RBRACKET);
		r = new PredicateCallAlloyFormula(isSuper,programName.getText(),parameterList,isStatic);
		return r;
	}
	
	public final QuantifiedFormula  quantifiedFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		QuantifiedFormula r;
		
		Token  id = null;
		
			Operator operator;
			AlloyFormula alloyFormula1;
			JType alloyType1;
			
			List<String> names = new ArrayList<String>();
			List<AlloyExpression> sets = new ArrayList<AlloyExpression>();	
		
		
		{
		switch ( LA(1)) {
		case LITERAL_some:
		{
			match(LITERAL_some);
			operator =   Operator.EXISTS;
			break;
		}
		case LITERAL_all:
		{
			match(LITERAL_all);
			operator =  Operator.FOR_ALL;
			break;
		}
		case LITERAL_lone:
		{
			match(LITERAL_lone);
			operator = Operator.LONE;
			break;
		}
		case LITERAL_no:
		{
			match(LITERAL_no);
			operator =   Operator.NONE;
			break;
		}
		case LITERAL_one:
		{
			match(LITERAL_one);
			operator =  Operator.ONE;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		int _cnt67=0;
		_loop67:
		do {
			if ((LA(1)==IDENT)) {
				id = LT(1);
				match(IDENT);
				match(COLON);
				alloyType1=alloyType();
					
								names.add(id.getText()); 
								sets.add(alloyType1.toAlloyExpr()); 
								AlloyVariable variable = AlloyVariable.buildAlloyVariable(id.getText());
								ctx.addAlloyVariable(variable);	
							
			}
			else {
				if ( _cnt67>=1 ) { break _loop67; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt67++;
		} while (true);
		}
		match(PIPE);
		match(LBRACE);
		alloyFormula1=alloyFormula(ctx);
		match(RBRACE);
		r = new QuantifiedFormula(operator, names, sets, alloyFormula1);
		return r;
	}
	
	public final AlloyFormula  equalsFormula(
		JDynAlloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyFormula r;
		
		
			AlloyExpression left; 
			AlloyExpression right; 
		
		
		left=termExpression(ctx);
		match(EQUALS);
		right=termExpression(ctx);
		
				r = new EqualsFormula(left, right); 
			
		return r;
	}
	
	public final FormalParametersDeclaration  variablesDeclaration() throws RecognitionException, TokenStreamException {
		FormalParametersDeclaration r;
		
		r = new FormalParametersDeclaration();
		
		{
		switch ( LA(1)) {
		case IDENT:
		{
			variablesDeclarationSingleType(r);
			break;
		}
		case EOF:
		case COMMA:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop77:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				variablesDeclarationSingleType(r);
			}
			else {
				break _loop77;
			}
			
		} while (true);
		}
		return r;
	}
	
	public final void variablesDeclarationSingleType(
		FormalParametersDeclaration r
	) throws RecognitionException, TokenStreamException {
		
		List<String> vars = null; String t = null;
		
		vars=variables();
		match(COLON);
		t=typeName();
		ExpressionParser.extendAlloyTyping(r, vars, t);
	}
	
	public final List<String>  variables() throws RecognitionException, TokenStreamException {
		List<String> r;
		
		Token  id = null;
		Token  id2 = null;
		r = new LinkedList<String>();
		
		id = LT(1);
		match(IDENT);
		r.add(id.getText());
		{
		_loop81:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				id2 = LT(1);
				match(IDENT);
				r.add(id2.getText());
			}
			else {
				break _loop81;
			}
			
		} while (true);
		}
		return r;
	}
	
	public final String  multiplicity() throws RecognitionException, TokenStreamException {
		String r;
		
		r = null;
		
		switch ( LA(1)) {
		case LITERAL_lone:
		{
			match(LITERAL_lone);
			r = "lone";
			break;
		}
		case LITERAL_one:
		{
			match(LITERAL_one);
			r = "one";
			break;
		}
		case LITERAL_some:
		{
			match(LITERAL_some);
			r = "some";
			break;
		}
		case LITERAL_seq:
		{
			match(LITERAL_seq);
			r = "seq";
			break;
		}
		case LITERAL_set:
		{
			match(LITERAL_set);
			r = "set";
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return r;
	}
	
	public final List<AlloyExpression>  actualParams(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		List<AlloyExpression> r;
		
		r = new LinkedList<AlloyExpression>(); AlloyExpression p;
		
		{
		switch ( LA(1)) {
		case IDENT:
		case LPAREN:
		case NUMBER:
		case TRUE:
		case FALSE:
		{
			p=termExpression(ctx);
			r.add(p);
			break;
		}
		case COMMA:
		case RBRACKET:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		_loop91:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				p=termExpression(ctx);
				r.add(p);
			}
			else {
				break _loop91;
			}
			
		} while (true);
		}
		return r;
	}
	
	public final AlloyExpression  joinExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		r=productExpression(ctx);
		{
		switch ( LA(1)) {
		case DOT:
		{
			match(DOT);
			other=joinExpression(ctx);
			r = new ExprJoin(r, other);
			break;
		}
		case EOF:
		case LITERAL_module:
		case LBRACE:
		case RBRACE:
		case OBJECT_INVARIANT:
		case CLASS_INVARIANT:
		case OBJECT_CONSTRAINT:
		case CLASS_CONSTRAINT:
		case REPRESENTS:
		case SUCH_THAT:
		case VIRTUAL:
		case PROGRAM:
		case COMMA:
		case RBRACKET:
		case SEMICOLON:
		case ASSIGNMENT:
		case LOOP_INVARIANT:
		case IMPLIES:
		case OR:
		case AND:
		case RPAREN:
		case EQUALS:
		case PLUS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return r;
	}
	
	public final AlloyExpression  productExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		r=overrideExpression(ctx);
		{
		switch ( LA(1)) {
		case RARROW:
		{
			match(RARROW);
			other=productExpression(ctx);
			r = new ExprProduct(r, other);
			break;
		}
		case EOF:
		case LITERAL_module:
		case LBRACE:
		case RBRACE:
		case OBJECT_INVARIANT:
		case CLASS_INVARIANT:
		case OBJECT_CONSTRAINT:
		case CLASS_CONSTRAINT:
		case REPRESENTS:
		case SUCH_THAT:
		case VIRTUAL:
		case PROGRAM:
		case COMMA:
		case RBRACKET:
		case SEMICOLON:
		case ASSIGNMENT:
		case LOOP_INVARIANT:
		case IMPLIES:
		case OR:
		case AND:
		case RPAREN:
		case EQUALS:
		case PLUS:
		case DOT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return r;
	}
	
	public final AlloyExpression  overrideExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		r=intersectionExpression(ctx);
		{
		switch ( LA(1)) {
		case PLUSPLUS:
		{
			match(PLUSPLUS);
			other=overrideExpression(ctx);
			r = new ExprOverride(r, other);
			break;
		}
		case EOF:
		case LITERAL_module:
		case LBRACE:
		case RBRACE:
		case OBJECT_INVARIANT:
		case CLASS_INVARIANT:
		case OBJECT_CONSTRAINT:
		case CLASS_CONSTRAINT:
		case REPRESENTS:
		case SUCH_THAT:
		case VIRTUAL:
		case PROGRAM:
		case COMMA:
		case RBRACKET:
		case SEMICOLON:
		case ASSIGNMENT:
		case LOOP_INVARIANT:
		case IMPLIES:
		case OR:
		case AND:
		case RPAREN:
		case EQUALS:
		case RARROW:
		case PLUS:
		case DOT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return r;
	}
	
	public final AlloyExpression  intersectionExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		r=atomicExpression(ctx);
		{
		switch ( LA(1)) {
		case AMPERSTAND:
		{
			match(AMPERSTAND);
			other=intersectionExpression(ctx);
			r = new ExprIntersection(r, other);
			break;
		}
		case EOF:
		case LITERAL_module:
		case LBRACE:
		case RBRACE:
		case OBJECT_INVARIANT:
		case CLASS_INVARIANT:
		case OBJECT_CONSTRAINT:
		case CLASS_CONSTRAINT:
		case REPRESENTS:
		case SUCH_THAT:
		case VIRTUAL:
		case PROGRAM:
		case COMMA:
		case RBRACKET:
		case SEMICOLON:
		case ASSIGNMENT:
		case LOOP_INVARIANT:
		case IMPLIES:
		case OR:
		case AND:
		case RPAREN:
		case EQUALS:
		case RARROW:
		case PLUS:
		case DOT:
		case PLUSPLUS:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return r;
	}
	
	public final AlloyExpression  atomicExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		Token  id = null;
		Token  number = null;
		r = null;
		
		switch ( LA(1)) {
		case NUMBER:
		{
			number = LT(1);
			match(NUMBER);
			r = ExpressionParser.buildTerm(ctx, number, null);
			break;
		}
		case LPAREN:
		{
			match(LPAREN);
			r=termExpression(ctx);
			match(RPAREN);
			break;
		}
		case TRUE:
		{
			match(TRUE);
			r = ExprConstant.buildExprConstant("true");
			break;
		}
		case FALSE:
		{
			match(FALSE);
			r = ExprConstant.buildExprConstant("false");
			break;
		}
		default:
			if ((LA(1)==IDENT) && (_tokenSet_31.member(LA(2)))) {
				id = LT(1);
				match(IDENT);
				r = ExpressionParser.buildTerm(ctx, id, null);
			}
			else if ((LA(1)==IDENT) && (LA(2)==LBRACKET||LA(2)==SLASH)) {
				r=functionExpression(ctx);
			}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return r;
	}
	
	public final AlloyExpression  functionExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		Token  aliasModuleId = null;
		Token  functionId = null;
		r = null; List<AlloyExpression> args = null;
		
		{
		if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
			aliasModuleId = LT(1);
			match(IDENT);
			match(SLASH);
		}
		else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		functionId = LT(1);
		match(IDENT);
		match(LBRACKET);
		args=actualParams(ctx);
		match(RBRACKET);
		r = new ExprFunction(aliasModuleId==null ? null : aliasModuleId.getText(),functionId.getText(), args);
		return r;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"module\"",
		"IDENT",
		"\"abstract\"",
		"\"sig\"",
		"\"extends\"",
		"\"in\"",
		"LBRACE",
		"RBRACE",
		"\"field\"",
		"COLON",
		"OBJECT_INVARIANT",
		"CLASS_INVARIANT",
		"OBJECT_CONSTRAINT",
		"CLASS_CONSTRAINT",
		"REPRESENTS",
		"SUCH_THAT",
		"VIRTUAL",
		"PROGRAM",
		"PURE",
		"LBRACKET",
		"COMMA",
		"RBRACKET",
		"SPECIFICATION",
		"IMPLEMENTATION",
		"SPECCASE",
		"SPECCASE_ID",
		"REQUIRES",
		"ENSURES",
		"MODIFIES",
		"ASSERT",
		"SEMICOLON",
		"IF",
		"ELSE",
		"ASSUME",
		"VAR",
		"SKIP",
		"CREATE_OBJECT",
		"LT",
		"GT",
		"ASSIGNMENT",
		"SUPER",
		"CALL",
		"WHILE",
		"LOOP_INVARIANT",
		"HAVOC",
		"IMPLIES",
		"OR",
		"AND",
		"LPAREN",
		"RPAREN",
		"NOT",
		"\"some\"",
		"\"all\"",
		"\"lone\"",
		"\"no\"",
		"\"one\"",
		"PIPE",
		"STATICCALLSPEC",
		"CALLSPEC",
		"EQUALS",
		"RARROW",
		"PLUS",
		"\"seq\"",
		"\"set\"",
		"SLASH",
		"DOT",
		"PLUSPLUS",
		"AMPERSTAND",
		"NUMBER",
		"TRUE",
		"FALSE",
		"STAR",
		"QUESTION",
		"COMMENT",
		"COMMENT_SLASH_SLASH",
		"COMMENT_ML",
		"WS"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 8056957325551863840L, 1792L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { -1166414711290867662L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { -9210274637110326254L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { -1152928171411121102L, 2019L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { -1152925903391556366L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { -1152925902720467726L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { -1152921504673955854L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { -67108878L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 4910324440368160L, 1792L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 1923037058117403682L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 1923443783131737122L, 1807L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 9219286243319741490L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { -4085793262404558L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { -4081394678498062L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { -7516193550L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 8056957325551861792L, 1792L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { -1166414711294525406L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { -1157407511989444574L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { -1153326107643614158L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { -15462889423822L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { -4398784709390L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 4503599677702176L, 1792L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 17592203286662194L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 8070435069358496818L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { -1153466862315495390L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { -404603036767182L, 2047L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 4503599627370528L, 1792L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { -9218868437219016670L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { -9209861237913944030L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { -9205779833568113614L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { -1152936967496278990L, 2035L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { -9210274637110326254L, 227L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	
	}
