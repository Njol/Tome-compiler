package ch.njol.brokkr.compiler;

import java.util.function.Consumer;

import ch.njol.brokkr.compiler.ast.Element;
import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.compiler.ast.Link;

public class Linker {
	
	public static void link(final Element ast, final Consumer<LinkerError> errors) {
		for (final Link<?> link : ast.links()) {
			if (link.getName() != null && link.get() == null) {
				errors.accept(new LinkerError(link));
			}
		}
		for (final ElementPart part : ast.parts()) {
			if (part instanceof Element) {
				link((Element) part, errors);
			}
		}
	}
	
}
