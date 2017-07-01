package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalVariable;
import ch.njol.brokkr.compiler.ast.Members.ConstructorFieldParameter;

/**
 * A local variable defined in Brokkr code. Always a definition, as local variables cannot be overridden in any way.
 */
public class InterpretedBrokkrLocalVariable extends AbstractInterpretedBrokkrVariable implements InterpretedVariableDefinition {
	
	public InterpretedBrokkrLocalVariable(FormalVariable var) {
		super(var);
	}
	
	@Override
	public InterpretedVariableDefinition definition() {
		return this;
	}
	
}
