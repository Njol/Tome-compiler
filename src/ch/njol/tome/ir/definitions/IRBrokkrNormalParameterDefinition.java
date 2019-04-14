package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.members.ASTSimpleParameter;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;

public class IRBrokkrNormalParameterDefinition extends AbstractIRBrokkrParameter implements IRParameterDefinition {
	
	public IRBrokkrNormalParameterDefinition(final ASTSimpleParameter param, final IRAttributeRedefinition attribute) {
		super(param, attribute);
	}
	
	@Override
	public IRBrokkrNormalParameterDefinition definition() {
		return this;
	}
	
	@Override
	public @Nullable InterpretedObject defaultValue(final InterpreterContext context) throws InterpreterException {
		final ASTExpression<?> defaultValueExpr = ((ASTSimpleParameter) ast).defaultValue;
		return defaultValueExpr == null ? null : defaultValueExpr.getIR().interpret(context);
	}
	
}
