package ch.njol.brokkr.common;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.AbstractASTElement;

public enum Optionality {
	
	OPTIONAL, REQUIRED;
	
	public static @Nullable Optionality parse(final AbstractASTElement<?> parent) {
		if (parent.try_("optional"))
			return OPTIONAL;
		if (parent.try_("required"))
			return REQUIRED;
		return null;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
