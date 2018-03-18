package ch.njol.brokkr.ir.uses;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.nativetypes.IRBrokkrTypeClassDefinition;

// TODO document (and rename?)
/**
 * The class that Type objects are an instance of
 * FIXME {@link IRBrokkrTypeClassDefinition}
 */
public class IRTypeUseClassUse extends AbstractIRTypeUse implements IRClassUse {
	
	private final IRTypeUse typeUse;
	
	public IRTypeUseClassUse(final IRTypeUse typeUse) {
		this.typeUse = typeUse;
	}
	
	@Override
	public IRContext getIRContext() {
		return typeUse.getIRContext();
	}
	
	// Type<X> is covariant in X
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRTypeUseClassUse && typeUse.equalsType(((IRTypeUseClassUse) other).typeUse);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRTypeUseClassUse) {
			return typeUse.compareTo(((IRTypeUseClassUse) other).typeUse);
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public int typeHashCode() {
		return typeUse.typeHashCode();
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
	public Set<? extends IRTypeUse> allInterfaces() {
		return new HashSet<>(Arrays.asList(getIRContext().getTypeDefinition("lang", "Type").getUse("T", typeUse, null), getIRContext().getTypeUse("lang", "Any")));
	}
	
	@Override
	public IRClassUse type() {
		return new IRTypeUseClassUse(this); // this can just be recursive
	}
	
	// TODO document
	public final static class IRTypeUseClassDefinition extends AbstractIRElement implements IRClassDefinition {
		
		private final IRTypeUseClassUse typeUseClassUse;
		
		private IRTypeUseClassDefinition(final IRTypeUseClassUse typeUseClassUse) {
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
		
//		@Override
//		public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
//			return getClass() == other.getClass() && typeUseClassUse.isSubtypeOfOrEqual(((IRTypeUseClassDefinition) other).typeUseClassUse);
//			// || other.??? ; // TODO subtype of the type 'Type' (with appropriate generic argument?)
//		}
		
		@Override
		public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Set<? extends IRTypeUse> allInterfaces() {
			return typeUseClassUse.allInterfaces();
		}
		
		@Override
		public IRContext getIRContext() {
			return typeUseClassUse.getIRContext();
		}
		
		@Override
		public int compareTo(final IRTypeDefinition other) {
			if (other instanceof IRTypeUseClassDefinition) {
				return typeUseClassUse.compareTo(((IRTypeUseClassDefinition) other).typeUseClassUse);
			}
			return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
			
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
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("not implemented"); // TODO
	}
	
	@Override
	public String toString() {
		return "Type<" + typeUse + ">";
	}
	
}
