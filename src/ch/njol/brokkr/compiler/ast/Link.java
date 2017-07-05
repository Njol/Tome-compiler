package ch.njol.brokkr.compiler.ast;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.interpreter.InterpreterException;

public abstract class Link<T> {
	
	// not 'parent' to not shadow 'parent' of Element
	public final Element parentElement;
	
	public Link(final Element parent) {
		this.parentElement = parent;
		parent.addLink(this);
	}
	
	public Link(final Element parent, @Nullable final WordToken name) {
		this(parent);
		setName(name);
	}
	
	private @Nullable WordToken name;
	
	public void setName(final @Nullable WordToken name) {
		this.name = name;
		value = null;
	}
	
	@SuppressWarnings("null")
	public @Nullable String getName() {
		return name != null ? name.word : null;
	}
	
	public @Nullable WordToken getNameToken() {
		return name;
	}
	
	private @Nullable T value = null;
	
	private boolean isLinking = false;
	
	@SuppressWarnings("null")
	public @Nullable T get() {
		if (isLinking) // recursion - abort
			return null;
		isLinking = true;
		if (value == null) {
			if (name == null) {
				value = null;
			} else {
				try {
					value = tryLink(name.word);
				} catch (NullPointerException | InterpreterException e) {
					System.err.println("interpreter error in link " + parentElement + "::" + name);
				}
			}
		}
		isLinking = false;
		return value;
	}
	
	public void clear() {
		name = null;
	}
	
	protected abstract @Nullable T tryLink(String name);
	
	@Override
	public String toString() {
		return "" + name;
	}
}
