package ch.njol.brokkr.ast;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.common.AbstractInvalidatable;
import ch.njol.brokkr.common.Cache;
import ch.njol.brokkr.common.Derived;
import ch.njol.brokkr.compiler.Token.WordToken;

public abstract class ASTLink<T extends Derived> extends AbstractInvalidatable {
	
	// not 'parent' to not shadow 'parent' of ASTElement when used an anonymous subclass inside an element
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
	
	public final void setName(final @Nullable WordToken name) {
		assert this.name == null;
		this.name = name;
	}
	
	public final @Nullable String getName() {
		final WordToken token = name;
		return token != null ? token.word : null;
	}
	
	public final @Nullable WordToken getNameToken() {
		return name;
	}
	
	private boolean isLinking = false;
	
	private final Cache<@Nullable T> cache = new Cache<>(() -> {
		if (isLinking) {// recursion - abort
			assert false : this;
			return null;
		}
		final WordToken token = name;
		if (token == null)
			return null;
		isLinking = true;
		final @Nullable T value = tryLink(token.word);
		isLinking = false;
		return value;
	});
	
	public final @Nullable T get() {
		@Nullable
		T value = cache.get();
		if (value == null) {
			if (isLinking) {// recursion - abort
				assert false : this;
				return null;
			}
			final WordToken token = name;
			if (token != null) {
				isLinking = true;
				value = tryLink(token.word);
				isLinking = false;
				if (value != null)
					registerInvalidateListener(value);
			}
		}
		return value;
	}
	
	protected abstract @Nullable T tryLink(String name);
	
	@Override
	public String toString() {
		return "" + name;
	}
	
}
