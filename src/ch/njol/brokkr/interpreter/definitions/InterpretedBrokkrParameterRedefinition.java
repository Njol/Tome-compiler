package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Members.ConstructorFieldParameter;

public class InterpretedBrokkrParameterRedefinition extends AbstractInterpretedBrokkrParameter {
	
	private final InterpretedParameterRedefinition overridden;
	
	public InterpretedBrokkrParameterRedefinition(FormalParameter param, InterpretedParameterRedefinition overridden) {
		super(param);
		this.overridden = overridden;
	}
	
	@Override
	public InterpretedParameterDefinition definition() {
		return overridden.definition();
	}
	
}
