package ch.njol.tome.compiler;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ir.IRElement;

/**
 * An object that has an AST element that should be show to the user when linked to this element.
 * <p>
 * {@link ASTElementPart}s must link to themselves. {@link IRElement}s should implement this interface only if it makes sense to do so, and link to a sensible AST element.
 */
public interface SourceCodeLinkable {
	
	public @Nullable ASTElementPart getLinked();
	
}
