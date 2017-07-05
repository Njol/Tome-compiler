package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.Expression;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Members.SimpleParameter;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;

public class InterpretedBrokkrNormalParameterRedefinition extends AbstractInterpretedBrokkrParameter {
	
	private final InterpretedParameterRedefinition overridden;
	
	public InterpretedBrokkrNormalParameterRedefinition(final SimpleParameter param, final InterpretedParameterRedefinition overridden, final InterpretedAttributeRedefinition attribute) {
		super(param, attribute);
		this.overridden = overridden;
	}
	
	@Override
	public InterpretedParameterDefinition definition() {
		return overridden.definition();
	}
	
	@Override
	public @Nullable InterpretedObject defaultValue(final InterpreterContext context) {
		final Expression defaultValueExpr = ((SimpleParameter) param).defaultValue;
		return defaultValueExpr == null ? null : defaultValueExpr.interpret(context);
	}
	
}
