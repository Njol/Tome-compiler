package ch.njol.brokkr.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.compiler.ParseError;
import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.interpreter.InterpretedObject;

public interface Element extends ElementPart {
	
	/**
	 * The parts (i.e. children) of this element. Each element in this list will have its {@link #parent()} set to this element.
	 */
	public List<ElementPart> parts();
	
	public void addLink(Link<?> link);
	
	public List<Link<?>> links();
	
	@Override
	public abstract String toString();
	
	@Override
	public default void forEach(final Consumer<ElementPart> function) {
		function.accept(this);
		for (final ElementPart p : parts())
			p.forEach(function);
	}
	
	public List<ParseError> fatalParseErrors();
	
	@SuppressWarnings("unchecked")
	public default <T extends Element> List<T> getDirectChildrenOfType(final Class<T> type) {
		final List<T> r = new ArrayList<>();
		for (final ElementPart part : parts()) {
			if (type.isInstance(part))
				r.add((T) part);
		}
		return r;
	}
	
}
