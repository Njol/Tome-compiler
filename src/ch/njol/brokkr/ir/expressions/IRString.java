package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.ast.ASTExpressions.ASTString;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeString;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRString extends AbstractIRExpression {
	
	private final IRContext irContext;
	private final String value;
	
	public IRString(final ASTString ast) {
		irContext = ast.getIRContext();
		value = registerDependency(ast).value;
	}
	
	@Override
	public IRTypeUse type() {
		return irContext.getTypeUse("lang", "String");
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) {
		return new InterpretedNativeString(irContext, value); // FIXME return a non-native object that actually implements lang.String
	}
	
}
