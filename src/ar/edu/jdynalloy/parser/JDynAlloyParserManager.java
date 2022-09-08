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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.parser.JDynAlloyParser;
import ar.edu.jdynalloy.parser.JDynAlloyParsingException;
import ar.uba.dc.rfm.alloy.AlloyVariable;

public class JDynAlloyParserManager {

	private JDynAlloyParserManager() {

	}

	public static JDynAlloyModule parseFile(String file)
			throws JDynAlloyParsingException, FileNotFoundException {
		try {
			return parse(new FileReader(file));
		} catch (RecognitionException e) {
			throw new JDynAlloyParsingException(e, file);
		} catch (TokenStreamException e) {
			throw new JDynAlloyParsingException(e, file);
		}

	}

	public static JDynAlloyModule parse(Reader reader)
			throws RecognitionException, TokenStreamException {
		JDynAlloyParser parser;
		parser = createParser(reader);
		JDynAlloyProgramParseContext ctx = createContext();
		return parser.dynJmlAlloyModule(ctx);
	}

	public static JDynAlloyModule parseResource(String resourceName)
			throws JDynAlloyParsingException {
		try {
			InputStreamReader inputStreamReader = createReaderFromResource(resourceName);
			return parse(inputStreamReader);
		} catch (RecognitionException e) {
			throw new JDynAlloyParsingException(e, resourceName);
		} catch (TokenStreamException e) {
			throw new JDynAlloyParsingException(e, resourceName);
		}
	}

	public static List<JDynAlloyModule> parseModulesFile(String file,
			JDynAlloyProgramParseContext ctx) throws JDynAlloyParsingException,
			FileNotFoundException {
		return parseModulesFile(file, ctx);
	}

	public static List<JDynAlloyModule> parseModulesFile(String file)
			throws JDynAlloyParsingException, FileNotFoundException {
		try {
			return parseModules(new FileReader(file), null);
		} catch (RecognitionException e) {
			throw new JDynAlloyParsingException(e, file);
		} catch (TokenStreamException e) {
			throw new JDynAlloyParsingException(e, file);
		}
	}

	public static List<JDynAlloyModule> parseModules(Reader reader,
			JDynAlloyProgramParseContext ctx) throws RecognitionException,
			TokenStreamException {
		JDynAlloyParser parser;
		parser = createParser(reader);
		if (ctx != null) {
			parser.setGlobalCtx(ctx);
		}

		return parser.dynJmlAlloyModules();
	}

	public static List<JDynAlloyModule> parseModulesResource(
			String resourceName, JDynAlloyProgramParseContext ctx)
			throws JDynAlloyParsingException {
		try {
			InputStreamReader inputStreamReader = createReaderFromResource(resourceName);
			return parseModules(inputStreamReader, ctx);
		} catch (RecognitionException e) {
			throw new JDynAlloyParsingException(e, resourceName);
		} catch (TokenStreamException e) {
			throw new JDynAlloyParsingException(e, resourceName);
		}
	}

	public static List<JDynAlloyModule> parseModulesResource(String resourceName)
			throws JDynAlloyParsingException {
		return parseModulesResource(resourceName, null);
	}

	public static InputStreamReader createReaderFromResource(
			String resourceName) {
		InputStreamReader inputStreamReader;

		String resourceNameWithSlashes = resourceName;
		int lastDot = resourceNameWithSlashes.lastIndexOf('.');
		if (lastDot >= 0) {
			String extension = resourceNameWithSlashes.substring(lastDot + 1);
			resourceNameWithSlashes = resourceNameWithSlashes.substring(0,
					lastDot);
			resourceNameWithSlashes.replace('.', '/');
			resourceNameWithSlashes = resourceNameWithSlashes + "." + extension;
		} else {
			resourceNameWithSlashes.replace('.', '/');
		}

		InputStream inputStream = JDynAlloyParserManager.class.getClassLoader()
				.getResourceAsStream(resourceName);
		if (inputStream == null) {
			throw new JDynAlloyParsingException("Resource not found: "
					+ resourceName);
		}
		inputStreamReader = new InputStreamReader(inputStream);
		return inputStreamReader;
	}

	private static JDynAlloyParser createParser(Reader reader) {
		JDynAlloyParser parser;
		JDynAlloyLexer lexer = new JDynAlloyLexer(reader);
		parser = new JDynAlloyParser(lexer);
		ASTFactory factory = new ASTFactory();
		factory.setASTNodeClass(JDynAlloyAST.class);
		parser.setASTFactory(factory);
		return parser;
	}

	private static JDynAlloyProgramParseContext createContext() {
		List<AlloyVariable> ctxVariables = new ArrayList<AlloyVariable>();
		List<AlloyVariable> ctxFields = new ArrayList<AlloyVariable>();
		JDynAlloyProgramParseContext ctx = new JDynAlloyProgramParseContext(
				ctxVariables, ctxFields, true);
		return ctx;
	}

}
