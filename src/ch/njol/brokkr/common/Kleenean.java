package ch.njol.brokkr.common;

import java.util.Locale;

public enum Kleenean {
	TRUE, FALSE, UNKNOWN;
	
	final String stringValue;
	
	private Kleenean() {
		stringValue = "" + name().toLowerCase(Locale.ENGLISH);
	}
	
	@Override
	public String toString() {
		return stringValue;
	}
	
	public Kleenean negated() {
		return this == TRUE ? FALSE : this == UNKNOWN ? UNKNOWN : TRUE;
	}
	
	public Kleenean and(final Kleenean other) {
		return this == TRUE && other == TRUE ? TRUE : this == FALSE || other == FALSE ? FALSE : UNKNOWN;
	}
	
	public Kleenean or(final Kleenean other) {
		return this == TRUE || other == TRUE ? TRUE : this == FALSE && other == FALSE ? FALSE : UNKNOWN;
	}
	
	public Kleenean implies(final Kleenean other) {
		return negated().or(other);
	}
	
}
