package ch.njol.brokkr.interpreter.uses;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;

public class InterpretedTypeUseClassUse implements InterpretedClassUse {
	
	private final InterpretedTypeUse typeUse;
	
	public InterpretedTypeUseClassUse(final InterpretedTypeUse typeUse) {
		this.typeUse = typeUse;
	}
	
	// Type<X> is covariant in X
	
	@Override
	public boolean equalsType(final InterpretedTypeUse other) {
		return getClass() == other.getClass() && typeUse.equalsType(((InterpretedTypeUseClassUse) other).typeUse);
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final InterpretedTypeUse other) {
		return getClass() == other.getClass() && typeUse.isSubtypeOfOrEqual(((InterpretedTypeUseClassUse) other).typeUse);
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final InterpretedTypeUse other) {
		return getClass() == other.getClass() && typeUse.isSupertypeOfOrEqual(((InterpretedTypeUseClassUse) other).typeUse);
	}
	
	@Override
	public InterpretedClassUse nativeClass() {
		return new InterpretedTypeUseClassUse(this); // this can just be recursive
	}
	
	private final static class InterpretedTypeUseClassDefinition implements InterpretedNativeClassDefinition {
		
		private final InterpretedTypeUseClassUse typeUseClassUse;
		
		public InterpretedTypeUseClassDefinition(final InterpretedTypeUseClassUse typeUseClassUse) {
			this.typeUseClassUse = typeUseClassUse;
		}
		
		@Override
		public List<? extends InterpretedMemberRedefinition> members() {
			// TODO members? static one of the type, plus some more?
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public boolean equalsType(final InterpretedNativeTypeDefinition other) {
			return getClass() == other.getClass() && typeUseClassUse.equalsType(((InterpretedTypeUseClassDefinition)other).typeUseClassUse);
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final InterpretedNativeTypeDefinition other) {
			return getClass() == other.getClass() && typeUseClassUse.isSubtypeOfOrEqual(((InterpretedTypeUseClassDefinition)other).typeUseClassUse);
					// || other.??? ; // TODO subtype of the type 'Type' (with appropriate generic argument?)
		}
		
		@Override
		public @Nullable InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public InterpretedNativeClassDefinition getBase() {
		// return something that implements Type<[typeUse]>
		return new InterpretedTypeUseClassDefinition(this);
	}
	
	@Override
	public List<? extends InterpretedMemberUse> members() {
		return Collections.EMPTY_LIST; // TODO
	}
	
}
