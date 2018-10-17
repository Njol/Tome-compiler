package ch.njol.tome.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.toplevel.ASTSourceFile;
import ch.njol.tome.common.ContentAssistProposal;
import ch.njol.tome.compiler.SemanticError;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.util.StringMatcher;

public interface ASTElement extends ASTElementPart {
	
	public default ASTElement root() {
		final ASTElement parent = parent();
		return parent == null ? this : parent.root();
	}
	
	/**
	 * The parts (i.e. children) of this element. Each element in this list will have its {@link #parent()} set to this element.
	 * <p>
	 * This list is unmodifiable, and reflects changes to the children of this element dynamically.
	 */
	public List<? extends ASTElementPart> parts();
	
	/**
	 * Inserts an element as a child of this element, at the specified position.
	 * 
	 * @param child The element to add as a child
	 * @param index From 0 (insert as first child) to <tt>{@link #parts()}.size()</tt>, inclusive (insert as last child, same as {@link #addChild(ASTElementPart) addChild}).
	 */
	void insertChild(ASTElementPart child, int index);
	
	/**
	 * Adds an element as new last child of this element
	 */
	public default void addChild(final ASTElementPart child) {
		assert child.parent() != this; // this is almost definitely an error
		child.removeFromParent();
		insertChild(child, parts().size());
		assert child.parent() == this;
	}
	
	public default void addChildren(final List<? extends ASTElementPart> children) {
		for (final ASTElementPart child : children) {
			addChild(child);
		}
	}
	
	/**
	 * Removes a child node from this tree.
	 */
	void removeChild(ASTElementPart child);
	
	void clearChildren();
	
	default boolean containsChild(final ASTElementPart ast) {
		return parts().stream().anyMatch(p -> p == ast);
	}
	
	@Override
	default int regionLength() {
		return parts().stream().mapToInt(p -> p.regionLength()).sum();
	}
	
	public default @Nullable ASTDocument<?> document() {
		final ASTElement parent = parent();
		if (parent != null)
			return parent.document();
		return null;
	}
	
	@Override
	public abstract String toString();
	
	/**
	 * @param token The hovered token
	 * @return Information to display on hover, or null for none.
	 */
	default public @Nullable String hoverInfo(final Token token) {
		return null;
	}
	
	/**
	 * @param token The token where the content assist was executed on, or the left one if it was done between two tokens.
	 * @param matcher A matcher to filter resulting proposals
	 * @return A {@link Stream} of proposals
	 */
	default public @Nullable Stream<ContentAssistProposal> getContentAssistProposals(final Token token, final StringMatcher matcher) {
		return null;
	}
	
	@Override
	public default void forEach(final Consumer<ASTElementPart> function) {
		function.accept(this);
		for (final ASTElementPart p : parts())
			p.forEach(function);
	}
	
	@SuppressWarnings("unchecked")
	public default <T extends ASTElement> List<T> getDirectChildrenOfType(final Class<T> type) {
		final List<T> r = new ArrayList<>();
		for (final ASTElementPart part : parts()) {
			if (type.isInstance(part))
				r.add((T) part);
		}
		return r;
	}
	
	/**
	 * Invalidates this element and any of its ancestors or descendants.
	 * <p>
	 * This method is used on the topmost AST element that was changed on editing, as any ancestors and descendants may be created anew or changed (in particular, on the
	 * {@link ASTSourceFile} or {@link ASTModule} if the whole file was modified somehow, e.g. deleted or reloaded).
	 * <p>
	 * TODO maybe, instead of just assuming this behaviour, the elements actually affected on a change should just be unlinked (still unlinking a whole subtree at once though)
	 */
	default void invalidateSubtreeAndParents() {
		invalidateSubtree();
		invalidateParents();
	}
	
	/**
	 * Invalidates this element and any descendants.
	 */
	default void invalidateSubtree() {
		invalidateSelf();
		for (final ASTElementPart part : parts()) {
			if (part instanceof ASTElement)
				((ASTElement) part).invalidateSubtree();
		}
	}
	
	/**
	 * @param t A token that is a descendant of this element
	 * @return An object to link to, or null if the token does not represent anything linkable.
	 */
	public default @Nullable SourceCodeLinkable getLinked(final Token t) {
		final ASTElement parent = parent();
		return parent != null ? parent.getLinked(t) : null;
	}
	
	public default void getSemanticErrors(Consumer<SemanticError> consumer) {
//		final ASTElement parent = parent();
//		if (parent != null)
//			parent.getSemanticErrors(consumer);
	}
	
}
