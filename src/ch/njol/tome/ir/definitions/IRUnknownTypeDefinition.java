package ch.njol.tome.ir.definitions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ir.AbstractIRUnknown;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRUnknownTypeDefinition extends AbstractIRUnknown implements IRTypeDefinition {
	
	public IRUnknownTypeDefinition(final IRContext irContext, final String errorMessage, final @Nullable ASTElementPart location) {
		super(errorMessage, location, irContext);
	}
	
	@Override
	public List<? extends IRMemberRedefinition> members() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public List<? extends IRGenericParameter> genericParameters() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean equalsType(final IRTypeDefinition other) {
		return false;
	}
	
	@Override
	public int typeHashCode() {
		return 0;
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return Collections.singleton(getIRContext().getTypeUse("lang", "Any"));
	}
	
	@Override
	public int compareTo(final IRTypeDefinition other) {
		if (other instanceof IRUnknownTypeDefinition) {
			return 0;
		}
		return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public String toString() {
		return "<unknown type>";
	}
	
}
