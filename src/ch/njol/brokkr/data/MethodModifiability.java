package ch.njol.brokkr.data;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.AbstractElement;

public enum MethodModifiability {
	MODIFYING, NONMODIFYING;
	
	public static @Nullable MethodModifiability parse(final AbstractElement<?> parent) {
		final String val = parent.try_("modifying", "nonmodifying"); // "mod", "nonmod", removed due to meaning something else than the usual 'mod', which is 'modifiable', and applies to types instead of variables.
		if (val == null)
			return null;
		else if (val.startsWith("mod"))
			return MODIFYING;
		else
			return NONMODIFYING;
	}
	
	@Override
	public String toString() {
		return "" + name().toLowerCase(Locale.ENGLISH);
	}
}
