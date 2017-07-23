package ch.njol.brokkr.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ch.njol.brokkr.compiler.ParseError;

public interface ASTElement extends ASTElementPart {
	
	/**
	 * The parts (i.e. children) of this element. Each element in this list will have its {@link #parent()} set to this element.
	 */
	public List<ASTElementPart> parts();
	
	public void addLink(ASTLink<?> link);
	
	public List<ASTLink<?>> links();
	
	@Override
	public abstract String toString();
	
	@Override
	public default void forEach(final Consumer<ASTElementPart> function) {
		function.accept(this);
		for (final ASTElementPart p : parts())
			p.forEach(function);
	}
	
	public List<ParseError> fatalParseErrors();
	
	@SuppressWarnings("unchecked")
	public default <T extends ASTElement> List<T> getDirectChildrenOfType(final Class<T> type) {
		final List<T> r = new ArrayList<>();
		for (final ASTElementPart part : parts()) {
			if (type.isInstance(part))
				r.add((T) part);
		}
		return r;
	}
	
}
