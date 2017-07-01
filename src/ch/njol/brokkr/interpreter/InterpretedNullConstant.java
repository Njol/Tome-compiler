package ch.njol.brokkr.interpreter;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;
import ch.njol.brokkr.interpreter.uses.InterpretedClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public class InterpretedNullConstant implements InterpretedObject {
	
	public static class InterpretedNativeNullClass implements InterpretedNativeClassDefinition {
		
		@Override
		public @Nullable InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition) {
			return null;
			// basically a null pointer exception - shouldn't ever happen in correctly checked code
			//throw new InterpreterException("tried to get an attribute of a null value");
		}
		
		@Override
		public List<InterpretedMemberRedefinition> members() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public @Nullable InterpretedMemberRedefinition getMemberByName(final String name) {
			return null;
		}
		
		@Override
		public boolean equalsType(final InterpretedNativeTypeDefinition other) {
			return other instanceof InterpretedNativeNullClass;
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final InterpretedNativeTypeDefinition other) {
			// note: null is implemented not as a type, but as a value.
			// This means that this null type is not a subtype of all other types as in some other languages.
			return other instanceof InterpretedNativeNullClass;
		}
		
		@Override
		public boolean isSupertypeOfOrEqual(final InterpretedNativeTypeDefinition other) {
			return other instanceof InterpretedNativeNullClass;
		}
		
	}
	
	@Override
	public InterpretedClassUse nativeClass() {
		return new InterpretedSimpleClassUse(new InterpretedNativeNullClass());
	}
	
}
