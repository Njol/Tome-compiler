package ch.njol.brokkr.data;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.AbstractElement;

public enum Exclusivity {
	EXCLUSIVE, SHARDED;
	
	public static @Nullable Exclusivity parse(final AbstractElement<?> parent) {
		final String val = parent.try_("exclusive", "shared");
		if (val == null)
			return null;
		if (val.startsWith("e"))
			return EXCLUSIVE;
		else
			return SHARDED;
	}
}
