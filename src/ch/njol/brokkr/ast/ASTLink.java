package ch.njol.brokkr.ast;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.interpreter.InterpreterException;

public abstract class ASTLink<T> {
	
	// not 'parent' to not shadow 'parent' of Element
	public final ASTElement parentElement;
	
	public ASTLink(final ASTElement parent) {
		this.parentElement = parent;
		parent.addLink(this);
	}
	
	public ASTLink(final ASTElement parent, @Nullable final WordToken name) {
		this(parent);
		setName(name);
	}
	
	private @Nullable WordToken name;
	
	public void setName(final @Nullable WordToken name) {
		this.name = name;
		value = null;
	}
	
	public @Nullable String getName() {
		WordToken token = name;
		return token != null ? token.word : null;
	}
	
	public @Nullable WordToken getNameToken() {
		return name;
	}
	
	private @Nullable T value = null;
	
	private boolean isLinking = false;
	
	public @Nullable T get() {
		if (isLinking) // recursion - abort
			return null;
		isLinking = true;
		if (value == null) {
			WordToken token = name;
			if (token == null) {
				value = null;
			} else {
				try {
					value = tryLink(token.word);
				} catch (NullPointerException | InterpreterException e) {
//					System.err.println("interpreter error in link " + parentElement + "::" + name);
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
