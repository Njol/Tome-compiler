package ch.njol.tome.ir.definitions;

import ch.njol.tome.ir.IRElement;

public interface IRGenericParameter extends IRElement, Comparable<IRGenericParameter> {
	
	IRMemberRedefinition definition();
	
	IRTypeDefinition declaringType();
	
	String name();
	
	default boolean equalsGenericParameter(final IRGenericParameter other) {
		return declaringType().equalsType(other.declaringType()) && name().equals(other.name());
	}
	
	default int genericParameterHashCode() {
		final IRTypeDefinition declaringType = declaringType();
		return declaringType.typeHashCode() * 31 + name().hashCode();
	}
	
	@Override
	default int compareTo(final IRGenericParameter other) {
		final int typeC = declaringType().compareTo(other.declaringType());
		if (typeC != 0)
			return typeC;
		return name().compareTo(other.name());
	}
	
}
