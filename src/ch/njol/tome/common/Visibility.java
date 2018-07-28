package ch.njol.tome.common;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.parser.Parser;

public enum Visibility {
	PUBLIC, MODULE, PRIVATE;
	
	public static @Nullable Visibility parse(final Parser p) {
		final String val = p.try_("public", "module", "private");
		if (val == null)
			return null;
		return valueOf("" + val.toUpperCase(Locale.ENGLISH));
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
