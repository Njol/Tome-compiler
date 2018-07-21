package ch.njol.tome.compiler;

import java.util.function.Consumer;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.ASTLink;

public class Linker {
	
	public static void link(final ASTElement ast, final Consumer<LinkerError> errors) {
		for (final ASTLink<?> link : ast.links()) {
			if (link.getName() != null && link.get() == null) {
				errors.accept(new LinkerError(link));
			}
		}
		for (final ASTElementPart part : ast.parts()) {
			if (part instanceof ASTElement) {
				link((ASTElement) part, errors);
			}
		}
	}
	
}
