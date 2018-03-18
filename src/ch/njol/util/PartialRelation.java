package ch.njol.util;

public enum PartialRelation {
	LESS, EQUAL, GREATER, INCOMPARABLE;
	
	public final boolean isLessThanOrEqual() {
		return this == LESS || this == EQUAL;
	}
	
	public final boolean isLargeThanOrEqual() {
		return this == GREATER || this == EQUAL;
	}
	
}
