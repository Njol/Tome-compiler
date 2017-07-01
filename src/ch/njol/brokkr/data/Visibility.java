package ch.njol.brokkr.data;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.AbstractElement;

public enum Visibility {
	PUBLIC, MODULE, PRIVATE;
	
	public static @Nullable Visibility parse(final AbstractElement<?> parent) {
		final String val = parent.try_("public", "module", "private");
		if (val == null)
			return null;
		return valueOf("" + val.toUpperCase(Locale.ENGLISH));
	}
}
