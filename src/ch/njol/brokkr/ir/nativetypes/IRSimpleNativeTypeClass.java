package ch.njol.brokkr.ir.nativetypes;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

/**
 * The class of a type that is not handled specially (currently only tuples and native types are different)
 */
public class IRSimpleNativeTypeClass implements IRNativeTypeClass {
	
	private final IRTypeDefinition interpretedNativeType;
	
	public IRSimpleNativeTypeClass(final IRTypeDefinition interpretedNativeType) {
		this.interpretedNativeType = interpretedNativeType;
	}
	
	@Override
	public @Nullable IRAttributeImplementation getAttributeImplementation(@NonNull final IRAttributeDefinition definition) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public @Nullable IRMemberRedefinition getMemberByName(@NonNull final String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean equalsType(final IRTypeDefinition other) {
		return other instanceof IRSimpleNativeTypeClass && interpretedNativeType.equalsType(((IRSimpleNativeTypeClass) other).interpretedNativeType);
	}
	
	@Override
	public int typeHashCode() {
		return interpretedNativeType.typeHashCode();
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(@NonNull final IRTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public List<? extends IRMemberRedefinition> members() {
		// TODO members of type?
		return Collections.EMPTY_LIST;
	}
	
}
