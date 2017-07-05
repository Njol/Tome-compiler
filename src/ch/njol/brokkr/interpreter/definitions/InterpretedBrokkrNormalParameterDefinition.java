package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.Expression;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Members.SimpleParameter;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;

public class InterpretedBrokkrNormalParameterDefinition extends AbstractInterpretedBrokkrParameter implements InterpretedParameterDefinition {
	
	public InterpretedBrokkrNormalParameterDefinition(final SimpleParameter param, final InterpretedAttributeRedefinition attribute) {
		super(param, attribute);
	}
	
	@Override
	public InterpretedBrokkrNormalParameterDefinition definition() {
		return this;
	}
	
	@Override
	public @Nullable InterpretedObject defaultValue(InterpreterContext context) {
		Expression defaultValueExpr = ((SimpleParameter)param).defaultValue;
		return defaultValueExpr == null ? null : defaultValueExpr.interpret(context);
	}
	
}
