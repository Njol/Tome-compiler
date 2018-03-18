package ch.njol.brokkr.common;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.AbstractASTElement;

public enum Visibility {
	PUBLIC, MODULE, PRIVATE;
	
	public static @Nullable Visibility parse(final AbstractASTElement<?> parent) {
		final String val = parent.try_("public", "module", "private");
		if (val == null)
			return null;
		return valueOf("" + val.toUpperCase(Locale.ENGLISH));
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
