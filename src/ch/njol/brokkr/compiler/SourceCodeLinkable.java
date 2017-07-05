package ch.njol.brokkr.compiler;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.ElementPart;

/**
 * A type that has a link to the source code.
 */
public interface SourceCodeLinkable {
	
	@Nullable
	ElementPart getLinked();
	
}
