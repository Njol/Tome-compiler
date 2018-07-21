package ch.njol.tome.parser;

import ch.njol.tome.ast.ASTElement;

public class ElementPlaceholder<E extends ASTElement> {
	
	private final Parser parent;
	
	public ElementPlaceholder(final Parser parent) {
		this.parent = parent;
	}
	
	public <T extends E> AttachedElementParser<T> startAttached(final T element) {
		return new AttachedElementParser<>(parent, element);
	}
	
	public <T extends E> DetachedElementParser<T> startDetached(final T element) {
		return new DetachedElementParser<>(parent.in, element);
	}
	
}
