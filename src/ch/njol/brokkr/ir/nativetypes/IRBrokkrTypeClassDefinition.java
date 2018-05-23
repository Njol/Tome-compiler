package ch.njol.brokkr.ir.nativetypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRBrokkrTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUseClassUse;

/**
 * The class of a Brokkr type, i.e. the class that implements {@code Type<X>} for a Brokkr class or interface X.
 * FIXME {@link IRTypeUseClassUse}
 */
public class IRBrokkrTypeClassDefinition extends AbstractIRElement implements IRTypeClassDefinition {
	
	private final IRBrokkrTypeDefinition definition;
	
	public IRBrokkrTypeClassDefinition(final IRBrokkrTypeDefinition definition) {
		this.definition = registerDependency(definition);
	}
	
	@Override
	public IRContext getIRContext() {
		return definition.getIRContext();
	}
	
	@Override
	public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public @Nullable IRMemberRedefinition getMemberByName(final String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean equalsType(final IRTypeDefinition other) {
		return other instanceof IRBrokkrTypeClassDefinition && definition.equalsType(((IRBrokkrTypeClassDefinition) other).definition);
	}
	
	@Override
	public int typeHashCode() {
		return definition.typeHashCode();
	}
	
	@Override
	public List<? extends IRMemberRedefinition> members() {
		// TODO members of {Type}
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return new HashSet<>(Arrays.asList(getIRContext().getTypeUse("lang", "Type")));
	}
	
	@Override
	public int compareTo(final IRTypeDefinition other) {
		if (other instanceof IRBrokkrTypeClassDefinition) {
			return definition.compareTo(((IRBrokkrTypeClassDefinition) other).definition);
		}
		return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
	}
	
}
