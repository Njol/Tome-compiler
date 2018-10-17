package ch.njol.tome.ir.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeImplementation;
import ch.njol.tome.ir.definitions.IRClassDefinition;

public class IRSimpleClassUse extends IRSimpleTypeUse implements IRClassUse {
	
	public IRSimpleClassUse(final IRClassDefinition type) {
		super(type);
	}
	
	@Override
	public IRClassDefinition getDefinition() {
		return (IRClassDefinition) super.getDefinition();
	}
	
	@Override
	public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
		return getDefinition().getAttributeImplementation(definition);
	}
	
}
