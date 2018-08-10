package ch.njol.tome.ast;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.util.AbstractModifiable;
import ch.njol.tome.util.Cache;
import ch.njol.tome.util.Derived;

public abstract class ASTLink<T extends Derived> extends AbstractModifiable {
	
	// not 'parent' to not shadow 'parent' of ASTElement when used an anonymous subclass inside an element
	public final ASTElement parentElement;
	
	public ASTLink(final ASTElement parent) {
		this.parentElement = parent;
		parent.addLink(this);
	}
	
	public ASTLink(final ASTElement parent, @Nullable final WordOrSymbols name) {
		this(parent);
		setName(name);
	}
	
	private @Nullable WordOrSymbols name;
	public final void setName(final @Nullable WordOrSymbols name) {
		assert this.name == null;
		this.name = name;
	}
	
	public final @Nullable String getName() {
		final WordOrSymbols token = name;
		return token != null ? token.wordOrSymbols() : null;
	}
	
	public final @Nullable WordOrSymbols getNameToken() {
		return name;
	}
	
	private boolean isLinking = false;
	
	private final Cache<@Nullable T> cache = new Cache<>(() -> {
		if (isLinking) {// recursion - abort
//			assert false : this;
			return null;
		}
		final WordOrSymbols token = name;
		if (token == null)
			return null;
		isLinking = true;
		final @Nullable T value = tryLink(token.wordOrSymbols());
		isLinking = false;
		return value;
	});
	
	public final @Nullable T get() {
		return cache.get();
	}
	
	protected abstract @Nullable T tryLink(String name);
	
	@Override
	public String toString() {
		return "" + name;
	}
	
	@Override
	public synchronized void modified() {
		super.modified();
	}
	
}
