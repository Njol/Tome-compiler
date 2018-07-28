package ch.njol.tome.common;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.parser.Parser;

public enum Modifiability {
	MODIFIABLE, UNMODIFABLE, IMMUTABLE;
	
	// note: unmodifiable is only used to override e.g. a parameter declared modifiable in a superinterface - alone it makes no sense (as it is the default).
	
	public static @Nullable Modifiability parse(final Parser p) {
		final String val = p.try_("mod", "modifiable", "immut", "immutable", "unmod", "unmodifiable");
		if (val == null)
			return null;
		if (val.startsWith("mod"))
			return MODIFIABLE;
		else if (val.startsWith("immut"))
			return IMMUTABLE;
		else if (val.startsWith("unmod"))
			return UNMODIFABLE;
		assert false : val;
		return null;
	}
	
	public static @Nullable Modifiability parse(final String val) {
		if (val.equals("mod") || val.equals("modifiable"))
			return MODIFIABLE;
		else if (val.equals("immut") || val.equals("immutable"))
			return IMMUTABLE;
		else if (val.equals("unmod") || val.equals("unmodifiable"))
			return UNMODIFABLE;
		return null;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
