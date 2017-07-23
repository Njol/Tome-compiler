package ch.njol.brokkr.ir.uses;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;

public class IRSimpleClassUse extends IRSimpleTypeUse implements IRClassUse {
	
	public IRSimpleClassUse(final IRClassDefinition type) {
		super(type);
	}
	
	public IRSimpleClassUse(final IRClassDefinition base, final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		super(base, genericArguments);
	}
	
	@Override
	public IRClassDefinition getBase() {
		return (IRClassDefinition) super.getBase();
	}
	
	@Override
	public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
		return getBase().getAttributeImplementation(definition);
	}
	
}
