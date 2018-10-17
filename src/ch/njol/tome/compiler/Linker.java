package ch.njol.tome.compiler;

import java.util.function.Consumer;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;

public class Linker {
	
	public static void link(final ASTElement ast, final Consumer<SemanticError> errorConsumer) {
		ast.getSemanticErrors(errorConsumer);
		for (final ASTElementPart part : ast.parts()) {
			if (part instanceof ASTElement) {
				link((ASTElement) part, errorConsumer);
			}
		}
	}
	
}
