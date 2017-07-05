package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Members.NormalResult;

public class InterpretedBrokkrResultDefinition extends AbstractInterpretedBrokkrResult implements InterpretedResultDefinition {

	public InterpretedBrokkrResultDefinition(NormalResult result, InterpretedAttributeRedefinition attribute) {
		super(result, attribute);
	}
	
	@Override
	public InterpretedBrokkrResultDefinition definition() {
		return this;
	}
	
}
