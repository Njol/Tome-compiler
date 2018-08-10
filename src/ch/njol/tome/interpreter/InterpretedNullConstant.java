package ch.njol.tome.interpreter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeImplementation;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRClassDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.uses.IRClassUse;
import ch.njol.tome.ir.uses.IRSimpleClassUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.util.AbstractDerived;

// FIXME is this even an object? it's the ABSENCE of an object, and it has no proper type either...
public class InterpretedNullConstant implements InterpretedObject {
	
	private final IRContext irContext;
	
	private InterpretedNullConstant(final IRContext irContext) {
		this.irContext = irContext;
	}
	
	/**
	 * null is a singleton to make reference comparisons much easier ([InterpretedObject a == InterpretedObject b] is then exactly reference equality)
	 * // TODO if I make value types, those need a different interface to implement (e.g. InterpretedReferenceObject and InterpretedValueObject) or I'll still need an equality
	 * method that throws an InterpretedException for value objects
	 */
	public static InterpretedNullConstant get(final IRContext irContext) {
		return new InterpretedNullConstant(irContext);
	}
	
	private class IRNativeNullClassDefinition extends AbstractDerived implements IRClassDefinition {
		
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
			return other instanceof IRNativeNullClassDefinition;
		}
		
		@Override
		public int compareTo(final IRTypeDefinition other) {
			return other instanceof IRNativeNullClassDefinition ? 0 : -1;
		}
		
//		@Override
//		public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
//			// note: null is implemented not as a type, but as a value.
//			// This means that this null type is not a subtype of all other types as in some other languages.
//			// TODO or is it?
//			return other instanceof IRNativeNullClass;
//		}
//
//		@Override
//		public boolean isSupertypeOfOrEqual(final IRTypeDefinition other) {
//			return other instanceof IRNativeNullClass;
//		}
		
		@Override
		public int typeHashCode() {
			return 0;
		}
		
		@Override
		public Set<? extends IRTypeUse> allInterfaces() {
			return Collections.EMPTY_SET; // TODO all interfaces that exist? none? only Any?
		}

		@Override
		public List<IRAttributeRedefinition> positionalGenericParameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public IRContext getIRContext() {
			return irContext;
		}
		
	}
	
	@Override
	public IRClassUse nativeClass() {
		return new IRSimpleClassUse(new IRNativeNullClassDefinition());
	}
	
}
