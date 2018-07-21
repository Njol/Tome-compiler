package ch.njol.tome.ast;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.util.PrettyPrinter;

public interface ASTElementPart extends SourceCodeLinkable {
	
	public @Nullable ASTElement parent();
	
	public static void assertValidParent(final ASTElementPart child, @Nullable final ASTElement parent) {
		assert parent != child; // TODO check for longer cycles as well
		final ASTElement existingParent = child.parent();
		assert existingParent == null || !existingParent.containsChild(child);
		assert parent == null || parent.containsChild(child);
	}
	
	/**
	 * Sets this element's parent.
	 * <p>
	 * This method is only intended to be called by {@link ASTElement#insertChild(ASTElementPart, int) ASTElement.insertChild}.
	 * <p>
	 * Implementation note: call {@link #assertValidParent(ASTElementPart, ASTElement) ASTElementPart.assertValidParent(this, parent)} at the beginning of the method.
	 * 
	 * @param parent The new parent or null to remove the parent (make this element a top level element or remove it from an existing tree)
	 */
	public void setParent(@Nullable ASTElement parent);
	
	default void removeFromParent() {
		ASTElement parent = parent();
		if (parent != null)
			parent.removeChild(this);
	}
	
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
	public default int absoluteRegionStart() {
		final ASTElement parent = parent();
		if (parent == null)
			return 0;
		return parent.absoluteRegionStart() + relativeRegionStart();
	}
	
	/**
	 * @return The end of this element (in characters from the start of the document), exclusive
	 */
	public default int absoluteRegionEnd() {
		final ASTElement parent = parent();
		if (parent == null)
			return relativeRegionEnd();
		return parent.absoluteRegionStart() + relativeRegionEnd();
	}
	
	/**
	 * @return The start of this element (in characters from the start of the parent element). If this element has no parent, returns 0.
	 */
	public default int relativeRegionStart() {
		final ASTElement parent = parent();
		if (parent == null)
			return 0;
		int start = 0;
		for (final ASTElementPart sibling : parent.parts()) {
			if (sibling == this)
				return start;
			start += sibling.regionLength();
		}
		assert false : "AST element part [" + this + "] is not a part of its parent element [" + parent + "]. All reported sibling elements: " + parent.parts();
		return -1;
	}
	
	/**
	 * @return The end of this element (in characters from the start of the parent element), exclusive. If this element has no parent, returns the length of this element.
	 */
	public default int relativeRegionEnd() {
		return relativeRegionStart() + regionLength();
	}
	
	/**
	 * @return Total length of this element in characters.
	 */
	public int regionLength();
	
	/**
	 * @return Start of the region to select when linked to this element, in characters from the start of the document
	 */
	public default int linkStart() {
		return absoluteRegionStart();
	}
	
	/**
	 * @return End of the region to select when linked to this element, in characters from the start of the document
	 */
	public default int linkEnd() {
		return absoluteRegionEnd();
	}
	
	/**
	 * @return Length of the region to select when linked to this element
	 */
	public default int linkLength() {
		return linkEnd() - linkStart();
	}
	
	/**
	 * Pretty print this element
	 * 
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
		return parent != null ? parent.getIRContext() : new IRContext("<missing parent in element " + this + " (" + getClass().getSimpleName() + ")>");
	}
	
}
