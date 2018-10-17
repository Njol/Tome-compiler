package ch.njol.tome.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRValueGenericArgument extends AbstractIRElement implements IRGenericArgument {
	
	private final InterpretedObject value;
	private final IRContext irContext;
	
	public IRValueGenericArgument(final InterpretedObject value, final IRContext irContext) {
		this.value = value;
		this.irContext = irContext;
	}
	
	public InterpretedObject getValue() {
		return value;
	}
	
	public static IRGenericArgument fromExpression(final ASTExpression expression) {
		return fromExpression(expression.getIR(), expression);
	}
	
	public static IRGenericArgument fromExpression(final IRExpression expression, @Nullable final ASTElementPart location) {
		try {
			final InterpretedObject value = expression.interpret(new InterpreterContext(expression.getIRContext(), null));
			return new IRValueGenericArgument(value, expression.getIRContext());
		} catch (final InterpreterException e) {
			return new IRUnknownGenericArgument("Invalid value (" + e.getMessage() + ")", location, expression.getIRContext());
		}
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
}
