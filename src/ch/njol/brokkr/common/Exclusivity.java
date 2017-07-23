package ch.njol.brokkr.common;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.AbstractASTElement;

public enum Exclusivity {
	/**
	 * An exclusive value has only one reference to it.
	 * <p>
	 * An exclusive value can be passed to another thread, even if it is mutable.
	 * <p>
	 * // TODO allow internal cross-references? e.g. a ListEntry could reference the List it comes from, but only if that is 'private'
	 */
	EXCLUSIVE,
	/**
	 * A shared value can have any amount of references to it.
	 */
	SHARED,
	/**
	 * A borrowed value cannot be stored for more than the current function (lambda, attribute, etc.)
	 * <p>
	 * // TODO make sure this definition is actually good
	 */
	BORROWED;
	
	public static @Nullable Exclusivity parse(final AbstractASTElement<?> parent) {
		final String val = parent.try_("exclusive", "shared", "borrowed");
		if (val == null)
			return null;
		if (val.startsWith("e"))
			return EXCLUSIVE;
		if (val.startsWith("s"))
			return SHARED;
		else
			return BORROWED;
	}
}
