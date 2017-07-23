package ch.njol.brokkr.interpreter;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;

public class InterpretedNullConstant implements InterpretedObject {
	
	public static class IRNativeNullClass implements IRClassDefinition {
		
		@Override
		public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
			return null;
			// basically a null pointer exception - shouldn't ever happen in correctly checked code
			//throw new InterpreterException("tried to get an attribute of a null value");
		}
		
		@Override
		public List<IRMemberRedefinition> members() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public @Nullable IRMemberRedefinition getMemberByName(final String name) {
			return null;
		}
		
		@Override
		public boolean equalsType(final IRTypeDefinition other) {
			return other instanceof IRNativeNullClass;
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
			// note: null is implemented not as a type, but as a value.
			// This means that this null type is not a subtype of all other types as in some other languages.
			return other instanceof IRNativeNullClass;
		}
		
		@Override
		public boolean isSupertypeOfOrEqual(final IRTypeDefinition other) {
			return other instanceof IRNativeNullClass;
		}
		
	}
	
	@Override
	public IRClassUse nativeClass() {
		return new IRSimpleClassUse(new IRNativeNullClass());
	}
	
}
