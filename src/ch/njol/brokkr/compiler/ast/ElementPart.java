package ch.njol.brokkr.compiler.ast;

import java.io.PrintStream;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;

public interface ElementPart extends SourceCodeLinkable {
	
	public @Nullable Element parent();
	
	/**
	 * Sets this element's parent.
	 * <p>
	 * If this element already has a parent, it is removed from that element's {@link #parts()} and added to the parts of the new parent. <b>Any other links must be changed
	 * manually!</b>
	 * 
	 * @param parent The new parent or null to remove the parent (make this element a top level element of remove it from an existing tree)
	 */
	public void setParent(@Nullable Element parent);
	
	@SuppressWarnings("unchecked")
	public default <T> @Nullable T getParentOfType(final Class<T> type) {
		Element e = parent();
		while (!type.isInstance(e) && e != null)
			e = e.parent();
		return (T) e;
	}
	
	public int regionStart();
	
	public int regionEnd();
	
	public default int regionLength() {
		return regionEnd() - regionStart();
	}
	
	public default int linkStart() {
		return regionStart();
	}
	
	public default int linkEnd() {
		return regionEnd();
	}
	
	public default int linkLength() {
		return linkEnd() - linkStart();
	}
	
	public void print(PrintStream out, String indent);
	
	/**
	 * Traverses this AST in a depth-first manner.
	 * 
	 * @param function
	 */
	public default void forEach(final Consumer<ElementPart> function) {
		function.accept(this);
	}
	
	@Override
	default ElementPart getLinked() {
		return this;
	}
	
}
