package ch.njol.brokkr.common;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.AbstractASTElement;

public enum MethodModifiability {
	MODIFYING, NONMODIFYING;
	
	public static @Nullable MethodModifiability parse(final AbstractASTElement<?> parent) {
		// TODO remove mod and nonmod? they meaning something else than the usual 'mod', which is 'modifiable', and apply to types instead of variables.
		// or alternatively remove all shorthands, and make the IDE intelligently complete them (i.e. if mod is written, it gets expanded to the correct version without even pressing ctrl+space)
		// or another alternative, allow them and by default they will be changed to their long version on save/format
		final String val = parent.try_("modifying", "nonmodifying", "mod", "nonmod");
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
