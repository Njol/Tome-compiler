package ch.njol.tome.common;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.parser.Parser;

public enum Exclusiveness {
	/**
	 * An exclusive value has only one reference to it.
	 * <p>
	 * An exclusive value can be passed to another thread, even if it is mutable.
	 * <p>
	 * // TODO allow internal cross-references? e.g. a ListEntry could reference the List it comes from, but only if that is 'private'
	 * -> can be solved by adding an intermediate object between the public list and the nodes which is shared among the list and its nodes
	 * (that object could also be used to describe the list and nodes as part of a special object graph (see below on some thoughts) (or the other way around: the public object
	 * could be the one))
	 */
	EXCLUSIVE,
	/**
	 * A shared value can have any amount of references to it.
	 */
	SHARED;
	
	public static @Nullable Exclusiveness parse(final Parser p) {
		final String val = p.try_("exclusive", "shared");
		if (val == null)
			return null;
		if (val.startsWith("e"))
			return EXCLUSIVE;
		else
			return SHARED;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
	
}
