package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalResult;
import ch.njol.brokkr.compiler.ast.Members.ConstructorFieldParameter;
import ch.njol.brokkr.compiler.ast.Members.NormalResult;

public class InterpretedBrokkrResultRedefinition extends AbstractInterpretedBrokkrResult {
	
	private final InterpretedResultRedefinition overridden;
	
	public InterpretedBrokkrResultRedefinition(NormalResult result, InterpretedResultRedefinition overridden) {
		super(result);
		this.overridden = overridden;
	}
	
	@Override
	public InterpretedResultDefinition definition() {
		return overridden.definition();
	}
	
}
