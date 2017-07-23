package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTExpression;
import ch.njol.brokkr.ast.ASTMembers.ASTSimpleParameter;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;

public class IRBrokkrNormalParameterRedefinition extends AbstractIRBrokkrParameter {
	
	private final IRParameterRedefinition overridden;
	
	public IRBrokkrNormalParameterRedefinition(final ASTSimpleParameter param, final IRParameterRedefinition overridden, final IRAttributeRedefinition attribute) {
		super(param, attribute);
		this.overridden = overridden;
	}
	
	@Override
	public IRParameterDefinition definition() {
		return overridden.definition();
	}
	
	@Override
	public @Nullable InterpretedObject defaultValue(final InterpreterContext context) {
		final ASTExpression defaultValueExpr = ((ASTSimpleParameter) param).defaultValue;
		return defaultValueExpr == null ? null : defaultValueExpr.interpret(context);
	}
	
}
