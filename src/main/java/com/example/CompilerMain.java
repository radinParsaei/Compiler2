package com.example;

import java.util.ArrayList;
import java.util.Objects;

public class CompilerMain {
	private static final SyntaxTree.Blocks empty = new SyntaxTree.Blocks();

	public static SyntaxTree.Block compile(Compiler compiler) {
		Parser parser = lex(compiler);
		if (parser == null) return empty;
		String pMap = null;
		// Until the program is fully compiled and the parser rules are not effective anymore
		while (!Objects.equals(pMap, parser.getMap())) {
			pMap = parser.getMap();
			compiler.parse(parser);
		}
		return compiler.afterParse(parser);
	}

	public static Parser lex(Compiler compiler) {
		Lexer lexer = new Lexer();
		Compiler.initLexer(lexer);
		ArrayList<Token> result = lexer.lex(compiler.getInputCode());
		Parser parser = new Parser(result);
		parser.purge("IGNORE");
		if (compiler.afterLex(parser)) return parser;
		return null;
	}
}
