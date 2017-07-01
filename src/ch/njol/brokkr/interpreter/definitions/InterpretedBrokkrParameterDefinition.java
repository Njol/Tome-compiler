package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;

public class InterpretedBrokkrParameterDefinition extends AbstractInterpretedBrokkrParameter implements InterpretedParameterDefinition {

	public InterpretedBrokkrParameterDefinition(FormalParameter param) {
		super(param);
	}
	
	@Override
	public InterpretedBrokkrParameterDefinition definition() {
		return this;
	}
	
}
