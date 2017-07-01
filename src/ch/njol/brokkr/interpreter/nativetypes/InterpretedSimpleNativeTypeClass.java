package ch.njol.brokkr.interpreter.nativetypes;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;

/**
 * The class of a type that is not handled specially (currently only tuples and native types are different)
 */
public class InterpretedSimpleNativeTypeClass implements InterpretedNativeTypeClass {
	
	private final InterpretedNativeTypeDefinition interpretedNativeType;
	
	public InterpretedSimpleNativeTypeClass(final InterpretedNativeTypeDefinition interpretedNativeType) {
		this.interpretedNativeType = interpretedNativeType;
	}
	
	@Override
	public @Nullable InterpretedAttributeImplementation getAttributeImplementation(@NonNull final InterpretedAttributeDefinition definition) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public @Nullable InterpretedMemberRedefinition getMemberByName(@NonNull final String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean equalsType(final InterpretedNativeTypeDefinition other) {
		return other instanceof InterpretedSimpleNativeTypeClass && ((InterpretedSimpleNativeTypeClass) other).interpretedNativeType.equalsType(interpretedNativeType);
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(@NonNull final InterpretedNativeTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<? extends InterpretedMemberRedefinition> members() {
		// TODO members of type?
		return Collections.EMPTY_LIST;
	}
	
}
