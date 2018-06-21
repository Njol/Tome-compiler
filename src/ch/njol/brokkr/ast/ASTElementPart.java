package ch.njol.brokkr.ast;

import java.io.PrintStream;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.util.PrettyPrinter;

public interface ASTElementPart extends SourceCodeLinkable {
	
	public @Nullable ASTElement parent();
	
	/**
	 * Sets this element's parent.
	 * <p>
	 * If this element already has a parent, it is removed from that element's {@link #parts()} and added to the parts of the new parent. <b>Any other links must be changed
	 * manually!</b>
	 * 
	 * @param parent The new parent or null to remove the parent (make this element a top level element or remove it from an existing tree)
	 */
	public void setParent(@Nullable ASTElement parent);
	
	@SuppressWarnings("unchecked")
	public default <T extends ASTElement> @Nullable T getParentOfType(final Class<T> type) {
		ASTElement e = parent();
		while (!type.isInstance(e) && e != null)
			e = e.parent();
		return (T) e;
	}
	
	@SuppressWarnings("unchecked")
	public default <T extends ASTElementPart> @Nullable T getParentOrSelfOfType(final Class<T> type) {
		ASTElementPart e = this;
		while (!type.isInstance(e) && e != null)
			e = e.parent();
		return (T) e;
	}
	
	/**
	 * @return The start of this element (in characters from the start of the document)
	 */
	public int regionStart();

	/**
	 * @return The end of this element (in characters from the start of the document), exclusive
	 */
	public int regionEnd();
	
	/**
	 * @return Total length of this element in characters
	 */
	public default int regionLength() {
		return regionEnd() - regionStart();
	}
	
	/**
	 * @return Start of the region to select when linked to this element
	 */
	public default int linkStart() {
		return regionStart();
	}

	/**
	 * @return End of the region to select when linked to this element
	 */
	public default int linkEnd() {
		return regionEnd();
	}

	/**
	 * @return Length of the region to select when linked to this element
	 */
	public default int linkLength() {
		return linkEnd() - linkStart();
	}
	
	/**
	 * Pretty print this element
	 * @param out 
	 */
	public void print(PrettyPrinter out);
	
	/**
	 * Traverses this AST in a depth-first manner, i.e. in the order in the source code usually.
	 * <p>
	 * An {@link ASTElement} is visited before its children are.
	 * 
	 * @param function
	 */
	public default void forEach(final Consumer<ASTElementPart> function) {
		function.accept(this);
	}
	
	@Override
	default @NonNull ASTElementPart getLinked() {
		return this;
	}
	
	default IRContext getIRContext() {
		final ASTElement parent = parent();
		return parent != null ? parent.getIRContext() : new IRContext();
	}
	
}
