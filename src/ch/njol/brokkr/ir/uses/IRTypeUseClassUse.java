package ch.njol.brokkr.ir.uses;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

// TODO document (and rename?)
public class IRTypeUseClassUse implements IRClassUse {
	
	private final IRTypeUse typeUse;
	
	public IRTypeUseClassUse(final IRTypeUse typeUse) {
		this.typeUse = typeUse;
	}
	
	// Type<X> is covariant in X
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return getClass() == other.getClass() && typeUse.equalsType(((IRTypeUseClassUse) other).typeUse);
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		return getClass() == other.getClass() && typeUse.isSubtypeOfOrEqual(((IRTypeUseClassUse) other).typeUse);
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		return getClass() == other.getClass() && typeUse.isSupertypeOfOrEqual(((IRTypeUseClassUse) other).typeUse);
	}
	
	@Override
	public IRClassUse nativeClass() {
		return new IRTypeUseClassUse(this); // this can just be recursive
	}
	
	private final static class IRTypeUseClassDefinition implements IRClassDefinition {
		
		private final IRTypeUseClassUse typeUseClassUse;
		
		public IRTypeUseClassDefinition(final IRTypeUseClassUse typeUseClassUse) {
			this.typeUseClassUse = typeUseClassUse;
		}
		
		@Override
		public List<? extends IRMemberRedefinition> members() {
			// TODO members? static one of the type, plus some more?
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public boolean equalsType(final IRTypeDefinition other) {
			return getClass() == other.getClass() && typeUseClassUse.equalsType(((IRTypeUseClassDefinition) other).typeUseClassUse);
		}
		
		@Override
		public int typeHashCode() {
			return typeUseClassUse.getBase().typeHashCode();
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
			return getClass() == other.getClass() && typeUseClassUse.isSubtypeOfOrEqual(((IRTypeUseClassDefinition) other).typeUseClassUse);
			// || other.??? ; // TODO subtype of the type 'Type' (with appropriate generic argument?)
		}
		
		@Override
		public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public IRClassDefinition getBase() {
		// return something that implements Type<[typeUse]>
		return new IRTypeUseClassDefinition(this);
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return Collections.EMPTY_LIST; // TODO
	}
	
}
