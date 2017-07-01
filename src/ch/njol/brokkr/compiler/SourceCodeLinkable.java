package ch.njol.brokkr.compiler;

import ch.njol.brokkr.compiler.ast.ElementPart;

/**
 * A type that has a link to the source code.
 */
public interface SourceCodeLinkable {
	
	ElementPart getLinked();
	
}
